#!/usr/bin/env python3

from flask import Flask, request, render_template
from flask_restplus import Resource, Api, fields, Model
from subprocess import run, PIPE
from hashlib import md5
from datetime import datetime

from dateutil.parser import isoparser

import logging
import json
import os
import time

app = Flask(__name__, template_folder='template')
api = Api(app)

isoparse = isoparser().isoparse

image_path = 'static'
image_url_prefix = 'http://127.0.0.1:5000/static/'

app.logger.setLevel(logging.DEBUG)

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

cook_data_model = api.model('cookDataModel', {
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
    'avatar': fields.String(
        required=True,
        description='推特用户头像URL'),
    'displayName': fields.String(
        required=True,
        description='推特用户显示名'),
    'username': fields.String(
        required=True,
        description='推特用户名（不含`@`）'),
    'postDate': fields.String(
        dt_format='iso8601',
        required=True,
        description='请求发出时间戳，ISO8601格式',
        example='2020-01-29T14:23:23.233+08:00'),
    'viewportWidth': fields.Integer(
        default=960,
        description='浏览器视口宽度（像素）'),
    'ppi': fields.Integer(
        default=96,
        description='生成图像PPI'),
    'serifFont': fields.Boolean(
        default=False,
        description='使用衬线字体渲染'),
})

cook_fields = base_fields.copy()
cook_fields['data'] = fields.Nested(
    cook_data_model, description='请求数据', required=True)


cook_model = api.model('cookRequestModel', cook_fields)

response_model = api.model('cookResponseModel', {
    'code': fields.Integer(description='返回码，0为成功，其他情况另外注明',
                           required=True),
    'message': fields.String(description='备注消息', required=True),
    'processTime': fields.Integer(description='处理用时（毫秒）'),
    'resultUrl': fields.String(description='输出图片URL')
})


@api.route('/api/cook')
class GenerateImage(Resource):
    @api.expect(cook_model, validate=True)
    @api.doc("test", model=response_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']
        app.logger.info(f'从{request_base_data["forwardFrom"]}'
                        f'收到任务{request_data["taskId"]}')
        filename = \
            md5(json.dumps(request_data).encode('utf-8')).hexdigest() + '.png'

        app.logger.debug(request_data['postDate'])
        cook_params = {
            'tid': request_data['tid'],
            'output_path': os.path.join(image_path, filename),
            'display_name': request_data['displayName'],
            'username': request_data['username'],
            'post_date': isoparse(request_data['postDate'])
        }

        if 'origText' in request_data or 'transText' in request_data:
            if 'origText' in request_data:
                cook_params['orig_text'] = request_data['origText']
            if 'transText' in request_data:
                cook_params['trans_text'] = request_data['transText']
        else:
            return make_response(400, '必须至少指定原文和译文之一')

        if 'media' in request_data:
            cook_params['media_urls'] = request_data['media']
        if 'tags' in request_data:
            cook_params['hashtags'] = request_data['tags']
        if 'viewportWidth' in request_data:
            cook_params['viewport_width'] = request_data['viewportWidth']
        if 'ppi' in request_data:
            cook_params['ppi'] = request_data['ppi']
        if 'serifFont' in request_data:
            cook_params['serif_font'] = request_data['serifFont']

        start_time = time.time()
        cook_result = cook_tweet(**cook_params)
        end_time = time.time()
        process_time_ms = int((end_time - start_time) * 1000)
        if not cook_result:
            return make_response(500, '后端生成图片发生错误，请联系管理员检查日志')
        else:
            result_url = image_url_prefix + filename
            return make_response(
                message='OK',
                resultUrl=result_url,
                processTime=process_time_ms
            )


def make_response(code=0, message="", **kwargs):
    response = {'code': code, 'message': message}
    response.update(kwargs)
    return response


def cook_tweet(
        tid, output_path, display_name, username, post_date, trans_text="",
        orig_text="", hashtags=[], media_urls=[], viewport_width=640, ppi=96,
        serif_font=False):
    zoom_ratio = ppi / 96
    command = ['wkhtmltoimage',
               '--zoom', str(zoom_ratio),
               '--width', str(viewport_width),
               '--disable-smart-width']

    post_date_formatted = post_date.strftime('%Y {} %m {} %d {} %H:%M:%S UTC%z')
    post_date_formatted = post_date_formatted.format('年', '月', '日')

    post_fields = {
        "display_name": display_name,
        "username": username,
        "post_date_formatted": post_date_formatted,
        "trans_text": trans_text,
        "orig_text": orig_text,
        "hashtags": json.dumps(hashtags),
        "media_urls": json.dumps(media_urls),
        "serif_font": json.dumps(serif_font)
    }

    for field, value in post_fields.items():
        command.append('--post')
        command.append(field)
        command.append(value)

    command.append('/internal/tweet_page')
    command.append(output_path)

    try:
        run_result = run(command, stdout=PIPE, stderr=PIPE)
    except FileNotFoundError:
        app.logger.error('找不到wkhtmltoimage可执行文件。')
        return False

    return True


@app.route('/internal/tweet_page', methods=['POST'])
def tweet_page():
    data = request.form

    rendered = render_template(
        'tweet_template.html',
        display_name=request.form['display_name'],
        username=request.form['username'],
        trans_text=request.form['trans_text'],
        orig_text=request.form['orig_text'],
        hashtags=json.loads(request.form['hashtags']),
        media_urls=json.loads(request.form['media_urls']),
        post_date_formatted=request.form['post_date_formatted'],
        serif_font=json.loads(request.form['serif_font'])
    )

    return rendered


if __name__ == '__main__':
    app.run()
