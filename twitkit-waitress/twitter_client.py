import tweepy
import config
import logging
import traceback

consumer_key = config.CONSUMER_KEY
consumer_secret = config.CONSUMER_SECRET
access_token_key = config.ACCESS_TOKEN_KEY
access_token_secret = config.ACCESS_TOKEN_SECRET


auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token_key, access_token_secret)

api = tweepy.API(auth)


def username_to_uid(username) -> int:
    user = api.get_user(screen_name=username)
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
        new_tweets_ = api.user_timeline(
            username, tweet_mode='extended', count=count, **range_args)
        max_count -= count
        new_tweets += new_tweets_
        if not new_tweets_ or max_count <= 0:
            break

    return new_tweets


class RealtimeUpdateStreamListener(tweepy.StreamListener):

    def __init__(self, uid, callback):
        """callback函数接受一个参数，为一个tweepy的Status对象"""
        self.callback = callback
        self.uid = uid
        self.api = api
        logging.info(f"开始监听推特用户{uid}")

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


def start_realtime_update(username, callback):
    uid = username_to_uid(username)
    rusl = RealtimeUpdateStreamListener(uid, callback)
    stream = tweepy.Stream(auth=api.auth, listener=rusl, tweet_mode='extended')
    stream.filter(follow=[str(uid)], is_async=True)
    return rusl
