
from flask import Blueprint, request, render_template
from subprocess import run, PIPE
from PIL import Image
from tid_code import add_code_to_image

import os
import sys
import logging
import json
import config


logger = logging.getLogger('app')
tweet_page_bp = Blueprint('internal', __name__, static_folder='baker/dist/')


def bake_tweet(tid, output_path,
               ppi=config.DEFAULT_PPI, transparent=False, smooth=True):

    payload_data = json.dumps({
        'tid': tid,
        'zh_font': config.ZH_FONT,
        'ja_font': config.JA_FONT,
    })

    zoom_ratio = ppi / 96
    if smooth:
        zoom_ratio *= 2

    viewport_width = config.VIEWPORT_WIDTH

    command = ['wkhtmltoimage',
               '--zoom', str(zoom_ratio),
               '--width', str(int(round(viewport_width * zoom_ratio))),
               '--disable-smart-width',
               '--cookie',
               'payload_data',
               payload_data]
    if transparent:
        command.append('--transparent')

    command.append(f'{config.INT_BASE_URL}/internal/index.html')
    command.append(output_path)

    try:
        os.makedirs(os.path.dirname(output_path), mode=0o755, exist_ok=True)
    except OSError:
        logger.error('创建输出目录失败。')
        return False
    try:
        run_result = run(command, stdout=PIPE, stderr=PIPE)
    except FileNotFoundError:
        logger.error('找不到wkhtmltoimage可执行文件。')
        return False

    if run_result.returncode != 0:
        logger.error(f'wkhtmltoimage执行失败，返回{run_result.returncode}。')
        logger.debug(run_result.stderr.decode(sys.stdout.encoding))
        logger.debug(run_result.stdout.decode(sys.stdout.encoding))
        return False

    render_ppi = int(zoom_ratio * 96)
    im = Image.open(output_path)
    add_code_to_image(im, tid,
                      config.TID_CODE_POS_X, config.TID_CODE_POS_Y,
                      config.TID_CODE_WIDTH, config.TID_CODE_HEIGHT)

    if smooth:
        # 如果前面wkhtmltoimage能写入图片，应该可以认为图片输出没问题，这里就不检查了。
        new_size = (int(round(im.size[0] / 2)), int(round(im.size[1] / 2)))
        im.thumbnail(new_size, resample=Image.BICUBIC)

    im.save(output_path)

    return True
