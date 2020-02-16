
from flask import Blueprint, request, render_template
from subprocess import run, PIPE
from twitter_util import username_to_displayname, username_to_avatar_url
from PIL import Image
from tid_code import add_code_to_image

import os
import sys
import logging
import json
import config


logger = logging.getLogger('app')
tweet_page_bp = Blueprint('internal', __name__, template_folder='template')


def bake_tweet(
        tid, output_path, username, post_date, retweeter_username=None,
        trans_text="", orig_text="", hashtags=[], media_urls=[],
        ppi=config.DEFAULT_PPI, transparent=False, smooth=True):

    post_date_formatted = post_date.strftime(
        '%Y {}!%m {}!%d {} %H:%M:%S (UTC%z)')
    post_date_formatted = post_date_formatted.format('年', '月', '日')
    post_date_formatted = post_date_formatted.replace('!0', ' ')
    post_date_formatted = post_date_formatted.replace('!', ' ')

    try:
        display_name = username_to_displayname(username)
        avatar_url = username_to_avatar_url(username)
        if retweeter_username:
            retweeter_avatar_url = username_to_avatar_url(retweeter_username)
        else:
            retweeter_avatar_url = ''
        if retweeter_username:
            retweeter_displayname = username_to_displayname(retweeter_username)
        else:
            retweeter_displayname = ''
    except Exception as e:
        logger.error(f'拉取推特用户信息时出错：{e}')
        return False

    if not retweeter_username:
        retweeter_username = ''

    payload_data = json.dumps({
        'display_name': display_name,
        'username': username,
        'avatar_url': avatar_url,
        'is_retweet': bool(retweeter_username),
        'retweeter_display_name': retweeter_displayname,
        'retweeter_username': retweeter_username,
        'retweeter_avatar_url': retweeter_avatar_url,
        'post_date_formatted': post_date_formatted,
        'trans_text': trans_text,
        'orig_text': orig_text,
        'hashtags': hashtags,
        'media_urls': media_urls
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

    command.append(f'{config.INT_BASE_URL}/internal/tweet_page')
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


@tweet_page_bp.route('/tweet_page', methods=['GET'])
def tweet_page():
    payload_data = json.loads(
        request.cookies['payload_data'])

    rendered = render_template(
        'tweet_template.html',
        zh_font = config.ZH_FONT,
        ja_font = config.JA_FONT,
        **payload_data
    )

    return rendered
