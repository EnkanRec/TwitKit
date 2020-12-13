#!/usr/bin/env python3

import requests
import config
import json
import logging
import uuid
import traceback
import time
import coloredlogs
import threading

from datetime import datetime, timezone
from rsshub_client import get_new_bilibili_status
from twitter_client import get_history_tweets, run_realtime_update
from twitter_util import batch_convert_tweepy_tweets, convert_tweepy_tweet


coloredlogs.install(
    level=logging.DEBUG if config.LOG_DEBUG else logging.INFO)

if config.LOG_FILE:
    log_handler = logging.FileHandler(config.LOG_FILE)
    logging.getLogger().addHandler(log_handler)


def make_request_payload(data):
    return {'forwardFrom': 'twitkit-maid',
            'timestamp': datetime.utcnow().isoformat(),
            'taskId': str(uuid.uuid4()),
            'data': data}


def validate_response(resp):
    if resp.status_code != 200:
        logging.debug(f'返回内容：{resp.text}')
        raise Exception(
            f'返回HTTP状态异常（{resp.status_code}）')
    resp_payload = json.loads(resp.text)
    if resp_payload['code'] != 0:
        if 'msg' in resp_payload:
            logging.error(resp_payload['msg'])
        if 'message' in resp_payload:
            logging.error(resp_payload['message'])
        raise Exception(
            f'返回码异常（{resp_payload["code"]}）')


def extract_response_data(resp):
    validate_response(resp)
    return json.loads(resp.text)['data']


def bulk_insert(fridge_tweets: list, full_ret=False):
    """此函数返回插入成功的tid列表，full_ret表示返回是否包括已存在、已过滤推送的"""
    known_tids = {}
    inserted_tweets = set()
    do_push_status_ids = set()
    logging.debug(f'准备插入{len(fridge_tweets)}条推')

    while True:
        to_be_inserted = []
        for tweet in fridge_tweets:
            if tweet['ref'] and tweet['ref'] not in known_tids.keys():
                continue
            if tweet['status_id'] in known_tids.keys():
                continue
            if tweet['ref']:
                tweet['ref'] = known_tids[tweet['ref']]
            # 有可能出现有重复时有的是过滤的有的没过滤的情况，以没过滤的优先
            if not tweet['is_push_filtered']:
                do_push_status_ids.add(tweet['status_id'])
            
            to_be_inserted.append(tweet)

        if not to_be_inserted:
            break

        logging.debug(f"准备插入：{to_be_inserted}")
        resp = requests.post(f'{config.FRIDGE_API_BASE}/db/task/bulk',
                             json=make_request_payload(to_be_inserted))
        inserted_tweets_actual = extract_response_data(resp)
        for tweet in inserted_tweets_actual:
            known_tids[tweet['twitter']['statusId']] = tweet['twitter']['tid']
            if full_ret or not tweet['alreadyExist'] and \
                    tweet['twitter']['statusId'] in do_push_status_ids:
                inserted_tweets.add(tweet['twitter']['tid'])
    return inserted_tweets


def separate_no_push_tweets(tweets):
    do_push = []
    no_push = []
    for tweet in tweets:
        if not config.PUSH_REPLIES and tweet['is_reply']:
            no_push.append(tweet)
        elif not config.PUSH_RETWEETS and tweet['is_retweet']:
            no_push.append(tweet)
        else:
            do_push.append(tweet)
    return do_push, no_push


def get_latest_status_id():
    resp = requests.post(f'{config.FRIDGE_API_BASE}/db/task/last',
                         json=make_request_payload({'withTranslation': False}))
    latest_tweet = extract_response_data(resp)
    if latest_tweet['twitter']:
        return int(latest_tweet['twitter']['statusId'])
    else:
        return None


class Maid:

    checked_image_urls = set()

    def __init__(self):
        payload = make_request_payload(['twid'])
        resp = requests.post(f'{config.FRIDGE_API_BASE}/db/kv/get',
                             json=payload)
        validate_response(resp)
        resp_data = extract_response_data(resp)
        if 'twid' not in resp_data or not resp_data['twid']:
            raise Exception('Fridge返回twid为空')
        self.monitor_target = resp_data['twid']

    def run_twitter_realtime_update(self):
        def update_tweets_from_stream(tweepy_tweet):
            try:
                self.update_tweets(convert_tweepy_tweet(tweepy_tweet))
            except:
                logging.error('执行推特更新时出错：')
                logging.error(traceback.format_exc())

        def update_history_tweets():
            time.sleep(1)
            latest_status_id = get_latest_status_id()
            new_tweets = batch_convert_tweepy_tweets(
                get_history_tweets(
                    self.monitor_target,
                    config.MAX_HISTORY_TWEETS, latest_status_id))
            self.update_tweets(new_tweets)

        backoff_timer = 1

        while True:
            try:
                last_time_start = time.time()
                threading.Thread(target=update_history_tweets).start()
                run_realtime_update(self.monitor_target,
                                    update_tweets_from_stream)
            except Exception as e:
                logging.warning(e)
                logging.warning(traceback.format_exc())
                if time.time() - last_time_start > 20:
                    backoff_timer = 1
                else:
                    backoff_timer *= 2
                logging.warning(f'尝试在{backoff_timer}秒内重新连接')
                time.sleep(backoff_timer)

    def update_tweets(self, new_tweets):
        new_tweets = new_tweets[::-1]
        new_tid_list = bulk_insert(new_tweets)
        if not new_tid_list:
            logging.debug('没有需要插入新推')
            return
        notify_data = {}
        for tid in new_tid_list:
            task_id = str(uuid.uuid4())
            logging.info(f'推文更新通知app：{task_id}: {tid}')
            notify_data[task_id] = tid
        try:
            resp = requests.post(f'{config.APP_API_BASE}/app/twitter',
                                 json=make_request_payload(notify_data))
            validate_response(resp)
            logging.info('成功通知APP')
        except Exception as e:
            logging.error(f'推文更新通知app失败：{e}')

    def check_tid(self, image_url):
        try:
            resp = requests.post(f'{config.OVEN_API_BASE}/oven/check',
                                 json=make_request_payload(
                                     {'imageUrl': image_url}))
            validate_response(resp)
            tid = json.loads(resp.text)['tid']
            return None if tid == -1 else tid
        except Exception as e:
            logging.error(f'解析图片tid时出错：{e}')
            return False

    def update_bilibili(self, items):
        for item in items:
            found_tid = set()
            for image_url in item.media_list:
                if image_url in self.checked_image_urls:
                    continue
                result = self.check_tid(image_url)
                if result is None:
                    self.checked_image_urls.add(image_url)
                if result:
                    found_tid.add(result)
            try:
                published_tid = self.set_published(found_tid)
                if published_tid:
                    self.notify_other(list(published_tid), item)
                self.checked_image_urls.update(item.media_list)
            except Exception as e:
                logging.warning(f'设置已发布时发生异常：{e}')
                logging.debug(traceback.format_exc())

    def set_published(self, tid_list):
        ret = set()
        for tid in tid_list:
            resp_get = requests.post(
                f'{config.FRIDGE_API_BASE}/db/task/get',
                json=make_request_payload({'tid': tid}))
            resp_get_data = extract_response_data(resp_get)
            if resp_get_data and not resp_get_data['twitter']['published']:
                resp_pub = requests.post(
                    f'{config.FRIDGE_API_BASE}/db/task/published',
                    json=make_request_payload({'tid': tid}))
            ret.add(tid)
        return ret

    def notify_other(self, tid_list, item):
        data = {
            'content': item.content,
            'url': item.url,
            'author': item.feed_title,
            'postDate': item.pub_date.isoformat()
        }
        if item.media_list:
            data['media'] = item.media_list
        if tid_list:
            data['tid'] = tid_list
        try:
            resp = requests.post(f'{config.APP_API_BASE}/app/other',
                                 json=make_request_payload(data))
            validate_response(resp)
            logging.info(f'通知APP其他更新成功（已发布：{tid_list})')
            return True
        except Exception as e:
            logging.error(f'通知APP其他更新时出错：{e}')
            return False

    def run_bilibili_update_checker(self):
        while True:
            start_time = time.time()
            try:
                self.update_bilibili(
                    get_new_bilibili_status(config.BILIBILI_UID))
            except:
                logging.error('执行Bilibili动态更新时出错：')
                logging.error(traceback.format_exc())
            elapsed_time = time.time() - start_time
            sleep_time = max(1, config.UPDATE_INTERVAL - elapsed_time)
            logging.debug(f'sleep for {sleep_time}s')
            time.sleep(sleep_time)
            logging.debug('sleep finished')

    def run(self):
        self.twitter_updater_thread = threading.Thread(
            target=self.run_twitter_realtime_update, args=(), daemon=True)
        self.twitter_updater_thread.start()

        if config.BILIBILI_UID:
            self.bilibili_updater_thread = threading.Thread(
                target=self.run_bilibili_update_checker, args=(), daemon=True)
            self.bilibili_updater_thread.start()
            self.bilibili_updater_thread.join()

        self.twitter_updater_thread.join()


if __name__ == '__main__':
    maid = Maid()
    maid.run()
