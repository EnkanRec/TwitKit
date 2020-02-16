#!/usr/bin/env python3

from flask_restplus import Resource, Api
from flask import request
from hashlib import md5
from datetime import datetime
from dateutil.parser import isoparser
from api_models import oven_api
from api_models import bake_model, bake_response_model
from api_models import check_model, check_response_model
from oven import bake_tweet
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


@oven_api.route('/bake')
class GenerateImage(Resource):
    @oven_api.expect(bake_model, validate=True)
    @oven_api.doc("bakeResponse", model=bake_response_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']
        task_id = request_base_data['taskId']

        try:
            parsed_post_date = isoparse(request_data['postDate'])
        except ValueError as e:
            return make_response(400, f'日期时间格式有误：{e}')

        logger.info(f'[{task_id}] '
                    f'从 {request_base_data["forwardFrom"]} 收到烤图任务')
        filename = \
            md5(json.dumps(request_data).encode('utf-8')).hexdigest() + '.png'

        bake_params = {
            'tid': request_data['tid'],
            'output_path': os.path.join(image_path, filename),
            'username': request_data['username'],
            'post_date': parsed_post_date
        }

        if 'origText' in request_data or 'transText' in request_data:
            if 'origText' in request_data:
                bake_params['orig_text'] = request_data['origText']
            if 'transText' in request_data:
                bake_params['trans_text'] = request_data['transText']
        else:
            return make_response(400, '必须至少指定原文和译文之一')

        if 'media' in request_data:
            bake_params['media_urls'] = request_data['media']
        if 'tags' in request_data:
            bake_params['hashtags'] = request_data['tags']
        if 'ppi' in request_data:
            bake_params['ppi'] = request_data['ppi']
        if 'retweeterUsername' in request_data:
            bake_params['retweeter_username'] = \
                request_data['retweeterUsername']

        start_time = time.time()
        bake_result = bake_tweet(**bake_params)
        end_time = time.time()
        process_time_ms = int((end_time - start_time) * 1000)
        if not bake_result:
            return make_response(500, '后端生成图片发生错误，请联系管理员检查日志')
        else:
            logger.info(f'[{task_id}] '
                        f'烤图任务完成，输出文件：{filename}')
            result_url = image_url_prefix + filename
            return make_response(
                message='OK',
                resultUrl=result_url,
                processTime=process_time_ms
            )


@oven_api.route('/check')
class GenerateImage(Resource):
    @oven_api.expect(check_model, validate=True)
    @oven_api.doc("CheckResponse", model=check_response_model)
    def post(self):
        request_base_data = request.json
        request_data = request_base_data['data']
        task_id = request_base_data['taskId']
        logger.info(f'[{task_id}] '
                    f'从 {request_base_data["forwardFrom"]} 收到检查tid任务')
        url = request_data['imageUrl']

        start_time = time.time()
        try:
            tid = read_code_from_image_url(
                url, config.TID_CODE_POS_X, config.TID_CODE_POS_Y,
                config.TID_CODE_WIDTH, config.TID_CODE_HEIGHT)
            message = 'OK'
            logger.info(f'[{task_id}] 检查tid任务完成（{tid}）。')
        except ValueError as e:
            logger.debug(f'[{task_id}] 二维码解码失败：{e}')
            tid = -1
            message = '没有找到有效的tid二维码'
        except Exception as e:
            logger.error(f'[{task_id}] 检查tid时发生了错误：{e}')
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
