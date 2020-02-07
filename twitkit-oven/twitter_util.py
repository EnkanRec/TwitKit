import requests
import re


def username_to_displayname(username):
    headers = {'User-Agent':
               'Mozilla/5.0 (compatible, MSIE 11, Windows '
               'NT 6.3; Trident/7.0;  rv:11.0) like Gecko'}
    try:
        response = requests.get(
            f'https://twitter.com/{username}', headers=headers)
    except requests.exceptions.RequestException as e:
        raise Exception(f"尝试获取用户显示名时出错：{e}")
    if response.status_code != 200:
        raise Exception(f"尝试获取用户显示名时返回异常：{response.status_code}")

    pattern = r'<title>(.*)\(@{}\)'.format(username)
    search_result = re.search(pattern, response.text)
    if not search_result:
        raise Exception(f"没有在title中找到用户显示名")
    return search_result.group(1)


def username_to_avatar_url(username):
    return f'https://avatars.io/twitter/{username}'
