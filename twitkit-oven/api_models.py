from flask_restplus import fields, Namespace
import config

oven_api = Namespace('api', description='烤推出图API')


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
    data_model = oven_api.model(f'{label}RequestDataModel', data_fields)

    req_fields = base_fields
    req_fields['data'] = fields.Nested(
        data_model, description='请求数据', required=True)

    return oven_api.model(f'{label}RequestModel', req_fields)


def make_response_model(resp_fields: dict, label: str):
    resp_fields.update({
        'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                               required=True),
        'message': fields.String(description='备注消息', required=True),
    })
    return oven_api.model(f'{label}ResponseModel', resp_fields)


bake_model = make_request_model({
    'tid': fields.Integer(
        description='任务ID（从任务列表烤推时，传此参数）',
        example=2233),
    'url': fields.String(
        description='推文URL（URL烤推时，传此参数）',
        example='https://twitter.com/magireco/status/1233776691064868865'),
    'transText': fields.String(
        description='推文译文，Plain Text格式，不传则使用数据库中的译文',
        example='译文'),
    'ppi': fields.Integer(
        default=config.DEFAULT_PPI,
        description='生成图像PPI',
        example=144)
}, 'bake')

check_model = make_request_model({
    'imageUrl': fields.String(
        required=True,
        description='要查询tid的图片的URL',
        example='http://localhost:5000/static/'
                '4f5b954b59360a1aeb8dfd9df0ed60ca.png')
}, 'check')

bake_response_model = make_response_model({
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'resultUrl': fields.String(description='输出图片URL')
}, 'bake')

check_response_model = make_response_model({
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'tid': fields.Integer(
        description='结果tid（仅处理成功时；若图片中不含有效的tid信息则返回-1）')
}, 'check')
