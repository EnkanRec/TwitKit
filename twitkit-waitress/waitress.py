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


def make_request_payload(data):
    return {'forwardFrom': 'twitkit-waitress',
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


def bulk_insert(fridge_tweets: list, include_existing=False):
    known_tid = {}
    inserted_tweets = set()
    logging.debug(f'准备插入{len(fridge_tweets)}条推')

    while True:
        to_be_inserted = []
        for tweet in fridge_tweets:
            if tweet['ref'] and tweet['ref'] not in known_tid.keys():
                continue
            if tweet['status_id'] in known_tid.keys():
                continue
            if tweet['ref']:
                tweet['ref'] = known_tid[tweet['ref']]
            to_be_inserted.append(tweet)

        if not to_be_inserted:
            break

        logging.debug(f"准备插入：{to_be_inserted}")
        resp = requests.post(f'{config.FRIDGE_API_ROOT}/db/task/bulk',
                             json=make_request_payload(to_be_inserted))
        inserted_tweets_actual = extract_response_data(resp)
        for tweet in inserted_tweets_actual:
            known_tid[tweet['twitter']['statusId']] = tweet['twitter']['tid']
            if include_existing or not tweet['alreadyExist']:
                inserted_tweets.add(tweet['twitter']['tid'])
    return inserted_tweets


def get_latest_status_id():
    resp = requests.post(f'{config.FRIDGE_API_ROOT}/db/task/last',
                         json=make_request_payload({'withTranslation': False}))
    latest_tweet = extract_response_data(resp)
    if latest_tweet['twitter']:
        return int(latest_tweet['twitter']['statusId'])
    else:
        return None


class Waitress:

    checked_image_urls = set()

    def __init__(self):
        payload = make_request_payload(['twid'])
        resp = requests.post(f'{config.FRIDGE_API_ROOT}/db/kv/get',
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

        while True:
            try:
                latest_status_id = get_latest_status_id()
                run_realtime_update(self.monitor_target,
                                    update_tweets_from_stream)

                new_tweets = batch_convert_tweepy_tweets(
                    get_history_tweets(
                        self.monitor_target,
                        config.MAX_HISTORY_TWEETS, latest_status_id))

                self.update_tweets(new_tweets)
            except Exception as e:
                logging.warning(e)
                logging.warning(traceback.format_exc)
                time.sleep(1)

    def update_tweets(self, new_tweets):
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
            resp = requests.post(f'{config.APP_API_ROOT}/app/twitter',
                                 json=make_request_payload(notify_data))
            validate_response(resp)
            logging.info('成功通知APP')
        except Exception as e:
            logging.error(f'推文更新通知app失败：{e}')

    def check_tid(self, image_url):
        try:
            resp = requests.post(f'{config.OVEN_API_ROOT}/oven/check',
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
                f'{config.FRIDGE_API_ROOT}/db/task/get',
                json=make_request_payload({'tid': tid}))
            resp_get_data = extract_response_data(resp_get)
            if resp_get_data and not resp_get_data['twitter']['published']:
                resp_pub = requests.post(
                    f'{config.FRIDGE_API_ROOT}/db/task/published',
                    json=make_request_payload({'tid': tid}))
            ret.add(tid)
        return ret

    def notify_other(self, tid_list, item):
        data = {
            'content': item.content,
            'url': item.url,
            'author': '？？？',  # TODO 确定作者
            'postDate': item.pub_date.isoformat()
        }
        if item.media_list:
            data['media'] = item.media_list
        if tid_list:
            data['tid'] = tid_list
        try:
            resp = requests.post(f'{config.APP_API_ROOT}/app/other',
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

        self.bilibili_updater_thread = threading.Thread(
            target=self.run_bilibili_update_checker, args=(), daemon=True)
        self.bilibili_updater_thread.start()

        self.twitter_updater_thread.join()
        self.bilibili_updater_thread.join()


if __name__ == '__main__':
    coloredlogs.install(
        level=logging.DEBUG if config.LOG_DEBUG else logging.INFO)

    waitress = Waitress()
    waitress.run()
