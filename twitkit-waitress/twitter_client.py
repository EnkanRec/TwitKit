import tweepy
import config
import logging
import traceback
import re

consumer_key = config.CONSUMER_KEY
consumer_secret = config.CONSUMER_SECRET
access_token_key = config.ACCESS_TOKEN_KEY
access_token_secret = config.ACCESS_TOKEN_SECRET


auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token_key, access_token_secret)

twitter_api = tweepy.API(auth)


def username_to_uid(username) -> int:
    user = twitter_api.get_user(screen_name=username)
    return user.id


def get_history_tweets(username, max_count=200, min_tweet_id=None):
    new_tweets = []
    while True:
        range_args = {}
        if min_tweet_id:
            range_args["since_id"] = min_tweet_id
        if new_tweets:
            range_args["max_id"] = new_tweets[-1].id - 1

        count = min(max_count, 200)
        new_tweets_ = twitter_api.user_timeline(
            username, tweet_mode='extended', count=count, **range_args)
        max_count -= count
        new_tweets += new_tweets_
        if not new_tweets_ or max_count <= 0:
            break

    return new_tweets


def get_tweet_by_id(id: int):
    return twitter_api.get_status(id, tweet_mode='extended')


def get_tweet_by_url(url):
    search_result = re.search(r'twitter.com\/[\w]{1,15}/status/([0-9]+)', url)
    if search_result:
        status_id = int(search_result.group(1))
    else:
        return False
    return get_tweet_by_id(status_id)


class RealtimeUpdateStreamListener(tweepy.StreamListener):

    def __init__(self, uid, callback):
        """callback函数接受一个参数，为一个tweepy的Status对象"""
        self.callback = callback
        self.uid = uid
        self.api = twitter_api
        logging.info(f"开始监听推特用户{uid}")
        super().__init__()

    def on_status(self, status):
        try:
            if status.user.id == self.uid:
                logging.info(f"收到实时动态：{status.text}")
                self.callback(status)
        except Exception as e:
            logging.warning(f"on_status发生错误：{e}")
            logging.warning(traceback.format_exc())

    def on_error(self, status_code):
        logging.warning(f"发生错误：{status_code}，重新连接。")
        return True


def run_realtime_update(username, callback):
    uid = username_to_uid(username)
    rusl = RealtimeUpdateStreamListener(uid, callback)
    stream = tweepy.Stream(
        auth=twitter_api.auth, listener=rusl, tweet_mode='extended')
    stream.filter(follow=[str(uid)], is_async=False)

