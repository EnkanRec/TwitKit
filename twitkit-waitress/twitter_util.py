from dateutil import tz
from datetime import datetime

import json
import re


def utc_to_local(dt: datetime):
    dt = dt.replace(tzinfo=tz.tzutc())
    dt = dt.astimezone(tz.tzlocal())
    return dt


def convert_tweepy_tweet(tweepy_tweet, two_level_format=False):
    ret = []

    def _convert_tweepy_tweet(tweepy_tweet):
        ref_tweet = None
        if hasattr(tweepy_tweet, 'full_text'):
            full_text = tweepy_tweet.full_text
        elif hasattr(tweepy_tweet, 'extended_tweet'):
            full_text = tweepy_tweet.extended_tweet['full_text']
        elif hasattr(tweepy_tweet, 'text'):
            full_text = tweepy_tweet.text

        full_text = re.sub(r' https:\/\/t.co\/[A-Za-z0-9]{10}$', '', full_text)

        ref_id = None

        media_urls = []
        if 'media' in tweepy_tweet.entities:
            for media in tweepy_tweet.entities['media']:
                media_urls.append(media['media_url'])

        if hasattr(tweepy_tweet, 'retweeted_status'):
            full_text = None
            ref_tweet = _convert_tweepy_tweet(tweepy_tweet.retweeted_status)
            ref_id = tweepy_tweet.retweeted_status.id
            media_urls = []
        elif hasattr(tweepy_tweet, 'quoted_status'):
            ref_tweet = _convert_tweepy_tweet(tweepy_tweet.quoted_status)
            ref_id = tweepy_tweet.quoted_status.id

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
            'extra': None,
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
                'extra': None,
            },
            'user': {
                'twitterUid': str(user.id),
                'name': user.screen_name,
                'display': user.screen_name,
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
