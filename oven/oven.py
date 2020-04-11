from flask import Blueprint, request
from PIL import Image
from tid_code import add_code_to_image
from hashlib import md5
from PIL import Image
from pdf2image import convert_from_bytes
from base64 import b64decode

import pychrome
import urllib.parse
import os
import sys
import logging
import json
import config
import traceback

browser = pychrome.Browser(url="http://127.0.0.1:9222")

logger = logging.getLogger('oven')
tweet_page_bp = Blueprint(
    'internal', __name__, static_folder='baker/dist/', static_url_path='/')

STATIC = 'static'


def bake_tweet(tid=None, url=None, trans_text=None, ppi=config.DEFAULT_PPI):

    if tid and url:
        logger.error('不能同时指定tid和url')
        return False

    try:
        os.makedirs(STATIC, mode=0o755, exist_ok=True)
    except OSError:
        logger.error('创建输出目录失败。')
        return False

    payload_data = json.dumps({
        'tid': tid,
        'url': url,
        'transText': trans_text,
        'zhFont': config.ZH_FONT,
        'jaFont': config.JA_FONT,
    })

    logger.debug(f'payload_data: {payload_data}')

    payload_data = urllib.parse.quote(payload_data, safe='')

    tab = browser.new_tab()
    tab.start()
    tab.Network.enable()

    im = None
    try:
        im = print_image_from_tab(tab, payload_data, ppi)
    except Exception as e:
        logger.error(f'输出图片失败：{e}')
        logger.error(traceback.format_exc)
    finally:
        tab.stop()
        browser.close_tab(tab)
    if not im:
        return False

    if not url:
        add_code_to_image(im, tid,
                          config.TID_CODE_POS_X, config.TID_CODE_POS_Y,
                          config.TID_CODE_WIDTH, config.TID_CODE_HEIGHT)

    actual_filename = f'{md5(im.tobytes()).hexdigest()}.png'
    im.save(os.path.join(STATIC, actual_filename))

    return actual_filename


def print_image_from_tab(tab, payload_data, ppi):

    loading_counter = []

    def request_will_be_sent(**kwargs):
        logger.debug(f"载入：{kwargs['request']['url']}")
        loading_counter.append(True)

    tab.Network.requestWillBeSent = request_will_be_sent

    def loading_finished(**kwargs):
        logger.debug('完成载入')
        loading_counter.pop()

    tab.Network.loadingFinished = loading_finished

    tab.Network.setCookie(
        name='payload_data',
        value=payload_data,
        url=config.INT_BASE_URL
    )

    tab.Page.navigate(url=f'{config.INT_BASE_URL}/internal/index.html')

    for _ in range(30):
        tab.wait(1)
        ready_state = tab.Runtime.evaluate(expression="document.readyState")[
            'result']['value']
        logger.debug(f'页面状态：{ready_state}')
        if ready_state == 'complete' and not loading_counter:
            break

    layout_metrics = tab.Page.getLayoutMetrics()
    content_size = layout_metrics['contentSize']

    pdf_data = b64decode(tab.Page.printToPDF(
        paperWidth=content_size['width']/96,
        paperHeight=content_size['height']/96,
        marginTop=0,
        marginBottom=0,
        marginLeft=0,
        marginRight=0,
        printBackground=True
    )['data'])

    return convert_from_bytes(pdf_data, single_file=True)[0]
