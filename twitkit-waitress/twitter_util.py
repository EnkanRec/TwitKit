from dateutil import tz
from datetime import datetime
from twitter_client import get_tweet_by_id

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
        if hasattr(tweepy_tweet, 'extended_tweet'):
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

        if hasattr(tweepy_tweet, 'retweeted_status'):
            full_text = None
            ref_tweet = _convert_tweepy_tweet(tweepy_tweet.retweeted_status)
            ref_id = tweepy_tweet.retweeted_status.id
            media_urls = []
        elif hasattr(tweepy_tweet, 'quoted_status'):
            ref_tweet = _convert_tweepy_tweet(tweepy_tweet.quoted_status)
            ref_id = tweepy_tweet.quoted_status.id
        elif hasattr(tweepy_tweet, 'in_reply_to_status_id') \
                and tweepy_tweet.in_reply_to_status_id:
            ref_id = tweepy_tweet.in_reply_to_status_id
            ref_tweet = _convert_tweepy_tweet(get_tweet_by_id(ref_id))

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
            'ref': str(ref_id) if ref_id else None
        } if not two_level_format else {
            'twitter': {
                'statusId': str(tweepy_tweet.id),
                'url': tweet_url,
                'content': full_text,
                'media': json.dumps(media_urls),
                'refStatusId': str(ref_id) if ref_id else None,
                'twitterUid': str(user.id),
                'pubDate': utc_to_local(tweepy_tweet.created_at).isoformat(),
                'extra': json.dumps(entities),
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
