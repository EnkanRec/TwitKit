#!/usr/bin/env python3

import requests
import config
import json
import logging
import uuid
import traceback
import time

from datetime import datetime, timezone
from rsshub_client import get_new_tweets, get_new_bilibili_status


def make_request_payload(data):
    return {'forwardFrom': 'twitkit-waitress',
            'timestamp': datetime.utcnow().replace(microsecond=0).isoformat() + '.111+00:00',
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


class Waitress:

    monitor_target = config.FALLBACK_TARGET
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

    def update_tweets(self):
        new_tweets = get_new_tweets(self.monitor_target)
        payload_data = []
        for tweet in new_tweets:
            payload_data.append({
                'url': tweet.url,
                'content': tweet.content,
                'media': json.dumps(tweet.media_list)
            })
        resp = requests.post(f'{config.FRIDGE_API_ROOT}/db/task/bulk',
                             json=make_request_payload(payload_data))
        resp_data = extract_response_data(resp)

        notify_data = {}
        for resp_tweet in resp_data:
            if resp_tweet['alreadyExist']:
                continue
            tid = resp_tweet['twitter']['tid']
            task_id = str(uuid.uuid4())
            logging.info(f'推文更新通知app：{task_id}: {tid}')
            notify_data[task_id] = tid
        if notify_data:
            try:
                resp = requests.post(f'{config.APP_API_ROOT}/app/twitter',
                                    json=make_request_payload(notify_data))
                validate_response(resp)
                logging.info('成功通知APP')
            except Exception as e:
                logging.error(f'推文更新通知app失败：{e}')
                for tid in notify_data.values():
                    del_resp = requests.post(
                        f'{config.FRIDGE_API_ROOT}/db/task/delete')
                    validate_response(del_resp)
                logging.info('已回滚')

    def check_tid(self, image_url):
        try:
            resp = requests.post(f'{config.OVEN_API_ROOT}/oven/check',
                                 json=make_request_payload({
                                     'imageUrl': image_url,
                                     'taskId': str(uuid.uuid4())}))
            validate_response(resp)
            tid = json.loads(resp.text)['tid']
            return None if tid == -1 else tid
        except Exception as e:
            logging.error(f'解析图片tid时出错：{e}')
            return False

    def update_bilibili(self):
        items = get_new_bilibili_status(config.BILIBILI_UID)
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
            'taskId': str(uuid.uuid4()),
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

    def run(self):
        while True:
            start_time = time.time()
            try:
                self.update_tweets()
            except:
                logging.error('执行推特更新时出错：')
                logging.error(traceback.format_exc())
            try:
                self.update_bilibili()
            except:
                logging.error('执行Bilibili动态更新时出错：')
                logging.error(traceback.format_exc())
            elapsed_time = time.time() - start_time
            time.sleep(max(0, config.UPDATE_INTERVAL - elapsed_time))


if __name__ == '__main__':
    logging.basicConfig(
        format='%(asctime)s [%(levelname)s] %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S',
        level=logging.DEBUG if config.LOG_DEBUG else logging.INFO,
        filename=config.LOG_FILE
    )
    waitress = Waitress()
    waitress.run()
