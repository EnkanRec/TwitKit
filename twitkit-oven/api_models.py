from flask_restplus import fields, Namespace
import config

oven_api = Namespace('api', description='烤推出图API')

base_fields = {
    'forwardFrom': fields.String(
        required=True,
        description='一个字符串，标定是哪个服务调用了此接口',
        example='twitkit-stargazer'),
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

bake_data_model = oven_api.model('bakeDataModel', {
    'tid': fields.Integer(
        required=True,
        description='推文ID',
        example=2233),
    'transText': fields.String(
        description='推文译文，Plain Text格式，不传则使用数据库中的译文',
        example='我有幸担任了魔法纪录新魔法少女“南津 凉子”的人物设计！请多多指教✨'),
    'ppi': fields.Integer(
        default=config.DEFAULT_PPI,
        description='生成图像PPI',
        example=144)
})

bake_fields = base_fields.copy()
bake_fields['data'] = fields.Nested(
    bake_data_model, description='请求数据', required=True)

bake_model = oven_api.model('bakeRequestModel', bake_fields)

check_data_model = oven_api.model('checkDataModel', {
    'imageUrl': fields.String(
        required=True,
        description='要查询tid的图片的URL',
        example='http://localhost:5000/static/'
                '4f5b954b59360a1aeb8dfd9df0ed60ca.png')
})

check_fields = base_fields.copy()
check_fields['data'] = fields.Nested(
    check_data_model, description='请求数据', required=True)

check_model = oven_api.model('checkRequestModel', check_fields)

bake_response_model = oven_api.model('bakeResponseModel', {
    'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                           required=True),
    'message': fields.String(description='备注消息', required=True),
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'resultUrl': fields.String(description='输出图片URL')
})

check_response_model = oven_api.model('checkResponseModel', {
    'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                           required=True),
    'message': fields.String(description='备注消息', required=True),
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'tid': fields.Integer(
        description='结果tid（仅处理成功时；若图片中不含有效的tid信息则返回-1）')
})
