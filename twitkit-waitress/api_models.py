from flask import Flask, make_response
from flask_restplus import Api, fields, Namespace
import config

app = Flask(__name__, root_path='/api/waitress')
api = Api(app, description='Waitress API')


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

    req_fields = base_fields.copy()
    req_fields['data'] = fields.Nested(
        data_fields, description='请求数据', required=True)

    bake_model = api.model(f'{label}RequestModel', req_fields)


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