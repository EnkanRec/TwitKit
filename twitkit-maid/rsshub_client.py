import dateutil.parser
import requests
import re

from collections import namedtuple
from xml.etree import ElementTree


RSSHUB_BILIBILI_URL = 'https://rsshub.app/bilibili/user/dynamic/{}'

FeedItem = namedtuple('FeedItem', 'feed_title content media_list url pub_date')


def extract_content_media(content):
    media_list = []
    for match in re.findall(r'(<br><img src="(.*?)".*?>)', content):
        media_list.append(match[1])
        content.replace(match[0], '')
    content = re.sub('<br>', '\n', content, flags=re.IGNORECASE)
    content = re.sub('<.*?>', '', content)
    return content.strip(), media_list


def get_new_feed(feed_url):
    r = requests.get(feed_url)
    if r.status_code != 200:
        raise Exception(f'RSSHub请求不成功，状态码：{r.status_code}')
    rss = ElementTree.fromstring(r.content)
    if rss.tag != 'rss':
        raise ValueError('RSS格式有误')
    channel = rss.find('channel')
    feed_title = channel.find('title').text
    ret = []
    for item in channel:
        if item.tag != 'item':
            continue
        content = item.find('description').text
        content, media_list = extract_content_media(content)
        pub_date = dateutil.parser.parse(item.find('pubDate').text)
        url = item.find('guid').text
        ret.append(FeedItem(
            feed_title=feed_title,
            content=content,
            media_list=media_list,
            url=url,
            pub_date=pub_date))
    return ret[::-1]


def get_new_bilibili_status(bilibili_uid):
    new_feed_items = get_new_feed(RSSHUB_BILIBILI_URL.format(bilibili_uid))
    return new_feed_items
