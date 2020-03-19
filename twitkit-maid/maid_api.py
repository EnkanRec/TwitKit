#!/usr/bin/env python3

from flask import Flask, make_response, request
from flask_restx import Api, fields, Namespace, Resource
from traceback import format_exc

import maid
import twitter_client
import twitter_util
import logging
import coloredlogs
import config

app = Flask(__name__)
api = Api(app, description='Maid API')


def make_request_model(data_fields: dict, label: str):
    base_fields = {
        'forwardFrom': fields.String(
            required=True,
            description='一个字符串，标定是哪个服务调用了此接口',
            example='twitkit-app'),
        'timestamp': fields.DateTime(
            dt_format='iso8601',
            required=True,
            description='请求发出时间戳，ISO8601格式',
            example='2020-01-29T14:23:23.233+08:00'),
        'taskId': fields.String(
            required=True,
            description='一个UUID，是一个任务的上下文唯一标识符',
            example='123e4567-e89b-12d3-a456-426655440000')
    }
    data_model = api.model(f'{label}RequestDataModel', data_fields)

    req_fields = base_fields
    req_fields['data'] = fields.Nested(
        data_model, description='请求数据', required=True)

    return api.model(f'{label}RequestModel', req_fields)


def make_response_model(resp_fields: dict, label: str):
    resp_fields.update({
        'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                               required=True),
        'message': fields.String(description='备注消息', required=True),
    })
    return api.model(f'{label}ResponseModel', resp_fields)


addtask_model = make_request_model({
    'url': fields.String(
        required=True,
        description='要加入到任务列表中的推文URL',
        example='https://twitter.com/magireco/status/1233776691064868865'),
}, 'addtask')

gettweet_model = make_request_model({
    'url': fields.String(
        required=True,
        description='要请求的推文URL',
        example='https://twitter.com/magireco/status/1233776691064868865'),
}, 'gettweet')

addtask_resp_model = make_response_model({
    'addedTid': fields.List(
        fields.String,
        description='已入库的任务的tid列表',
        example=[1001, 1002]),
    'rootTid': fields.Integer(
        description='请求插入的推文本身的tid',
        example=1001),
}, 'addtask')

gettweet_resp_model = make_response_model({
    'tweets': fields.Raw(
        description='得到的推文'),
    'rootStatusId': fields.Integer(
        description='请求插入的推文本身的推特推文ID',
        example=1001),
}, 'addtask')


def make_response(code=0, message="", **kwargs):
    response = {'code': code, 'message': message}
    response.update(kwargs)
    return response


@api.route('/api/maid/addtask')
class AddTask(Resource):
    @api.expect(addtask_model, validate=True)
    @api.doc("addtaskResponse", model=addtask_resp_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']
        task_label = "[{}][{}]".format(
            request_base_data['forwardFrom'], request_base_data['taskId'])

        logging.info(f'{task_label} 请求加入任务：{request_data["url"]}')

        try:
            tweet = twitter_client.get_tweet_by_url(request_data['url'])
        except Exception as e:
            logging.warning(f'{task_label} 请求推特API失败')
            logging.warning(format_exc())
            return make_response(500, '请求推特API失败')

        if not tweet:
            logging.warning(f'{task_label} 格式不正确')
            return make_response(400, 'URL格式不正确')

        try:
            inserted = maid.bulk_insert(
                twitter_util.convert_tweepy_tweet(tweet),
                include_existing=True)
        except Exception as e:
            logging.warning(f'{task_label} 请求Fridge失败')
            logging.warning(format_exc())
            return make_response(500, '请求Fridge失败')

        if not inserted:
            return make_response(500, '已插入列表为空')

        # root推文是最后插入的，所以tid最大
        root_tid = max(inserted)
        return make_response(
            message='OK',
            addedTid=list(inserted),
            rootTid=root_tid
        )


@api.route('/api/maid/gettweet')
class GetTweet(Resource):
    @api.expect(gettweet_model, validate=True)
    @api.doc("CheckResponse", model=gettweet_resp_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']
        task_label = "[{}][{}]".format(
            request_base_data['forwardFrom'], request_base_data['taskId'])

        logging.info(f'{task_label} 请求获取推文：{request_data["url"]}')

        try:
            tweet = twitter_client.get_tweet_by_url(request_data['url'])
        except Exception as e:
            logging.warning(f'{task_label} 请求推特API失败')
            logging.warning(format_exc())
            return make_response(500, '请求推特API失败')

        converted_tweets = twitter_util.convert_tweepy_tweet(
            tweet, two_level_format=True)

        root_status_id = converted_tweets[-1]['twitter']['statusId']
        resp_tweets = {}
        for t in converted_tweets:
            resp_tweets[t['twitter']['statusId']] = t

        return make_response(
            message='OK',
            tweets=resp_tweets,
            rootId=root_status_id
        )


@app.after_request
def after_request(response):
    if request.path.startswith('/api/maid'):
        try:
            response_data = json.loads(response.get_data())
        except:
            return response
        if 'code' not in response_data:
            response_data['code'] = response.status_code
            status_code = 200
            response.set_data(json.dumps(response_data))
    return response


if __name__ == '__main__':
    coloredlogs.install(
        level=logging.DEBUG if config.LOG_DEBUG else logging.INFO)
    app.run(debug=True, port=5001)
