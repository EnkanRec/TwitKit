from dateutil import tz
from datetime import datetime
from twitter_client import get_tweet_by_id
from tweepy.error import TweepError

import json
import re
import logging


def utc_to_local(dt: datetime):
    dt = dt.replace(tzinfo=tz.tzutc())
    dt = dt.astimezone(tz.tzlocal())
    return dt


def convert_tweepy_tweet(tweepy_tweet, two_level_format=False):
    ret = []

    def _convert_tweepy_tweet(tweepy_tweet):
        logging.debug(tweepy_tweet)
        ref_tweet = None
        if hasattr(tweepy_tweet, 'full_text'):
            full_text = tweepy_tweet.full_text
        elif hasattr(tweepy_tweet, 'extended_tweet'):
            full_text = tweepy_tweet.extended_tweet['full_text']
        else:
            full_text = tweepy_tweet.text

        full_text = re.sub(r' https:\/\/t.co\/[A-Za-z0-9]{10}$', '', full_text)

        ref_id = None

        media_urls = []
        entities_list = []
        if hasattr(tweepy_tweet, 'extended_entities'):
            entities_list.append(tweepy_tweet.extended_entities)
        elif hasattr(tweepy_tweet, 'extended_tweet') and \
                'extended_entities' in tweepy_tweet.extended_tweet:
            entities_list.append(
                tweepy_tweet.extended_tweet['extended_entities'])
        else:
            entities_list.append(tweepy_tweet.entities)

        entities = {}
        for e in entities_list:
            for k, v in e.items():
                if k not in entities:
                    entities[k] = v
                else:
                    if len(v) > len(entities[k]):
                        entities[k] = v

        if 'media' in entities:
            for media in entities['media']:
                media_urls.append(media['media_url_https'])

        is_retweet = False
        is_reply = False

        if hasattr(tweepy_tweet, 'retweeted_status'):
            is_retweet = True
            full_text = None
            _convert_tweepy_tweet(tweepy_tweet.retweeted_status)
            ref_id = tweepy_tweet.retweeted_status.id
            media_urls = []
        elif hasattr(tweepy_tweet, 'quoted_status'):
            _convert_tweepy_tweet(tweepy_tweet.quoted_status)
            ref_id = tweepy_tweet.quoted_status.id
        elif hasattr(tweepy_tweet, 'in_reply_to_status_id') \
                and tweepy_tweet.in_reply_to_status_id:
            is_reply = True
            ref_id = tweepy_tweet.in_reply_to_status_id
            try:
                _convert_tweepy_tweet(get_tweet_by_id(ref_id))
            except TweepError as e:
                if e.api_code == 144:
                    ret.append(make_dummy_tweet(
                        '找不到此推文，可能已删除', two_level_format, ref_id))
                else:
                    ret.append(make_dummy_tweet(
                        e.reason, two_level_format, ref_id))

        user = tweepy_tweet.user
        user_avatar_url = user.profile_image_url_https.replace(
            '_normal.jpg', '_400x400.jpg')
        tweet_url = 'https://twitter.com/' + \
            f'{user.screen_name}/status/{tweepy_tweet.id}'

        fridge_tweet = {
            'url': tweet_url,
            'content': full_text,
            'media': json.dumps(media_urls),
            'pub_date': utc_to_local(tweepy_tweet.created_at).isoformat(),
            'status_id': str(tweepy_tweet.id),
            'user_twitter_uid': str(user.id),
            'user_name': user.screen_name,
            'user_display': user.name,
            'user_avatar': user_avatar_url,
            'extra': json.dumps(entities),
            'ref': str(ref_id) if ref_id else None,
            'is_reply': is_reply,
            'is_retweet': is_retweet
        } if not two_level_format else {
            'is_reply': is_reply,
            'is_retweet': is_retweet,
            'twitter': {
                'statusId': str(tweepy_tweet.id),
                'url': tweet_url,
                'content': full_text,
                'media': json.dumps(media_urls),
                'refStatusId': str(ref_id) if ref_id else None,
                'twitterUid': str(user.id),
                'pubDate': utc_to_local(tweepy_tweet.created_at).isoformat(),
                'extra': json.dumps(entities)
            },
            'user': {
                'twitterUid': str(user.id),
                'name': user.screen_name,
                'display': user.name,
                'avatar': user_avatar_url
            }
        }
        ret.append(fridge_tweet)
    _convert_tweepy_tweet(tweepy_tweet)
    return ret


def batch_convert_tweepy_tweets(tweepy_tweets: list):
    ret = []
    for tweet in tweepy_tweets:
        converted = convert_tweepy_tweet(tweet)
        ret += converted
    return ret


def make_dummy_tweet(message, two_level_format=False, status_id=0):
    dummy_avatar = 'https://www.gravatar.com/avatar/' + \
        '00000000000000000000000000000000?d=mp&f=y'
    return {
        'url': 'n/a',
        'content': message,
        'media': json.dumps([]),
        'pub_date': datetime.now(tz.tzlocal()).isoformat(),
        'status_id': str(status_id),
        'user_twitter_uid': '0',
        'user_name': '**error**',
        'user_display': '错误',
        'user_avatar': dummy_avatar,
        'extra': json.dumps({}),
        'ref': None,
        'is_reply': False,
        'is_retweet': False
    } if not two_level_format else {
        'is_reply': False,
        'is_retweet': False,
        'twitter': {
            'statusId': str(status_id),
            'url': 'n/a',
            'content': message,
            'media': json.dumps([]),
            'refStatusId': None,
            'twitterUid': '0',
            'pubDate': datetime.now(tz.tzlocal()).isoformat(),
            'extra': json.dumps({}),
        },
        'user': {
            'twitterUid': '0',
            'name': '**error**',
            'display': '错误',
            'avatar': dummy_avatar
        }
    }
