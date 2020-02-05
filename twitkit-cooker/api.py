#!/usr/bin/env python3

from flask_restplus import Resource, Api
from flask import request
from hashlib import md5
from datetime import datetime
from dateutil.parser import isoparser
from api_models import cooker_api
from api_models import cook_model, cook_response_model
from api_models import check_model, check_response_model
from cooker import cook_tweet
from tid_code import read_code_from_image_url
from traceback import format_exc

import json
import os
import time
import logging
import config


isoparse = isoparser().isoparse

image_path = 'static'
image_url_prefix = f'{config.EXT_STATIC_BASE_URL}/'
logger = logging.getLogger('app')


@cooker_api.route('/cook')
class GenerateImage(Resource):
    @cooker_api.expect(cook_model, validate=True)
    @cooker_api.doc("CookResponse", model=cook_response_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']

        try:
            parsed_post_date = isoparse(request_data['postDate'])
        except ValueError as e:
            return make_response(400, f'日期时间格式有误：{e}')

        logger.info(f'从 {request_base_data["forwardFrom"]} '
                    f'收到烤图任务 {request_data["taskId"]} ')
        filename = \
            md5(json.dumps(request_data).encode('utf-8')).hexdigest() + '.png'

        cook_params = {
            'tid': request_data['tid'],
            'output_path': os.path.join(image_path, filename),
            'username': request_data['username'],
            'post_date': parsed_post_date
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
        if 'ppi' in request_data:
            cook_params['ppi'] = request_data['ppi']
        if 'retweeterUsername' in request_data:
            cook_params['retweeter_username'] = \
                request_data['retweeterUsername']

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


@cooker_api.route('/check')
class GenerateImage(Resource):
    @cooker_api.expect(check_model, validate=True)
    @cooker_api.doc("CheckResponse", model=check_response_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']
        logger.info(f'从 {request_base_data["forwardFrom"]} '
                    f'收到检查tid任务 {request_data["taskId"]} ')
        url = request_data['imageUrl']

        start_time = time.time()
        try:
            tid = read_code_from_image_url(
                url, config.TID_CODE_POS_X, config.TID_CODE_POS_Y,
                config.TID_CODE_WIDTH, config.TID_CODE_HEIGHT)
            message = 'OK'
        except ValueError as e:
            logger.warning(f'二维码解码失败：{e}')
            tid = -1
            message = '没有找到有效的tid二维码'
        except Exception as e:
            logger.error(f'发生了错误：{e}')
            logger.error(format_exc())
            return make_response(500, '检查tid失败，请联系管理员检查日志')

        end_time = time.time()
        process_time_ms = int((end_time - start_time) * 1000)

        return make_response(
            message=message, tid=tid, processTime=process_time_ms)


def make_response(code=0, message="", **kwargs):
    response = {'code': code, 'message': message}
    response.update(kwargs)
    return response
