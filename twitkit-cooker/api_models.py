from flask_restplus import fields, Namespace

cooker_api = Namespace('api', description='烤推出图API')

base_fields = {
    'forwardFrom': fields.String(
        required=True,
        description='一个字符串，标定是哪个服务调用了此接口',
        example='twitkit-stargazer'),
    'timestamp': fields.DateTime(
        dt_format='iso8601',
        required=True,
        description='请求发出时间戳，ISO8601格式',
        example='2020-01-29T14:23:23.233+08:00')
}

cook_data_model = cooker_api.model('cookDataModel', {
    'taskId': fields.String(
        required=True,
        description='一个UUID，是一个任务的上下文唯一标识符',
        example='123e4567-e89b-12d3-a456-426655440000'),
    'tid': fields.Integer(
        required=True,
        description='推文ID'),
    'origText': fields.String(
        description='推文原文，Plain Text格式，不传则只输出译文'),
    'transText': fields.String(
        description='推文译文，Plain Text格式，不传则只输出原文'),
    'media': fields.List(
        fields.String,
        description='媒体列表，列表里每个字符串是一个媒体URL'),
    'tags': fields.List(
        fields.String,
        description='标签列表，列表里每个字符串是一个标签（不含`#`）'),
    'username': fields.String(
        required=True,
        description='推特用户名（不含`@`）'),
    'retweeterUsername': fields.String(
        description='如果是转推，转推者的推特用户名（不含`@`）'),
    'postDate': fields.String(
        dt_format='iso8601',
        required=True,
        description='推文发出日期时间，ISO8601格式',
        example='2020-01-29T14:23:23.233+08:00'),
    'ppi': fields.Integer(
        default=96,
        description='生成图像PPI')
})

cook_fields = base_fields.copy()
cook_fields['data'] = fields.Nested(
    cook_data_model, description='请求数据', required=True)

cook_model = cooker_api.model('cookRequestModel', cook_fields)

check_data_model = cooker_api.model('checkDataModel', {
    'taskId': fields.String(
        required=True,
        description='一个UUID，是一个任务的上下文唯一标识符',
        example='123e4567-e89b-12d3-a456-426655440000'),
    'imageUrl': fields.String(
        required=True,
        description='要查询tid的图片的URL')
})

check_fields = base_fields.copy()
check_fields['data'] = fields.Nested(
    check_data_model, description='请求数据', required=True)

check_model = cooker_api.model('checkRequestModel', check_fields)

cook_response_model = cooker_api.model('cookResponseModel', {
    'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                           required=True),
    'message': fields.String(description='备注消息', required=True),
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'resultUrl': fields.String(description='输出图片URL')
})

check_response_model = cooker_api.model('checkResponseModel', {
    'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                           required=True),
    'message': fields.String(description='备注消息', required=True),
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'tid': fields.Integer(
        description='结果tid（仅处理成功时；若图片中不含有效的tid信息则返回-1）')
})