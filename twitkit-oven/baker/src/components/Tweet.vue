<template>
  <div class="tweet" :class="altBg ? 'altBackground' : 'normalBackground'">
    <div class="card" v-if="dataReady">
      <div class="cardTitle">
        <img class="avatar" :src="user.avatar" />
        <div class="userInfo">
          <div class="displayName" lang="ja-jp" v-html="user.display"></div>
          <div class="username">@{{ user.name }}</div>
        </div>
      </div>
      <div class="cardBody">
        <div
          v-html="translation ? translation.translation : tweet.content"
          :lang="translation ? null : 'ja-jp'"
        ></div>
        <div class="origText" v-if="translation && tweet.content">
          <legend>原文</legend>
          <div lang="ja-jp" v-html="tweet.content"></div>
        </div>
      </div>
      <div class="refTweet" v-if="tweet.refTid">
        <Tweet :tid="tweet.refTid" :alt-bg="!altBg" :no-date="true" />
      </div>
      <div class="media">
        <img v-for="url in JSON.parse(tweet.media)" :key="url" :src="url" />
      </div>
      <div class="postTime" v-if="!noDate">▶ 原推发表于 {{ formatDate(tweet.pubDate) }}</div>
    </div>
    <div v-html="errorMessage" v-if="errorMessage"></div>
  </div>
</template>

<script>
import axios from "axios";
import twemoji from "twemoji";
import escape from "escape-html";
import { v4 as uuidv4 } from "uuid";

export default {
  name: "Tweet",
  props: {
    tid: String,
    altBg: Boolean,
    noDate: Boolean
  },
  data() {
    return {
      tweet: null,
      user: null,
      translation: null,
      dataReady: false,
      errorMessage: null
    };
  },
  mounted() {
    axios
      .post("/fridge_api_proxy/db/task/get", {
        taskId: uuidv4(),
        forwardFrom: "twitkit-oven-baker",
        timestamp: (new Date).toISOString(),
        data: { tid: this.tid }
      })
      .then(response => {
        if (response.data.code != 0) {
          this.errorMessage = `Fridge返回${response.data.code}：${response.data.message}`;
          return;
        }

        this.tweet = response.data.data.twitter;
        this.user = response.data.data.user;
        this.translation = response.data.data.translation;
        if (this.tweet === null) {
          this.errorMessage = "推文不存在";
          return;
        }

        var convertEmoji = str => {
          return twemoji.replace(str, function(emoji) {
            return (
              `<img src="https://twemoji.maxcdn.com/svg/` +
              `${twemoji.convert.toCodePoint(emoji)}.svg" style="height:1em">`
            );
          });
        };

        var addColor = str => {
          str = str.replace(/(@\w{1,15})/g, '<span class="mention">$1</span>');
          str = str.replace(
            /(#[^\s,.!?，。！？<]*)/g,
            '<span class="hashtag">$1</span>'
          );
          str = str.replace(
            /([^"])(https?:\/\/[\w-_~:[\]@!$&'()*+,;=.?%#/]+)[^"]/g,
            '$1<span class="link">$2</span>'
          );
          return str;
        };

        if (this.tweet && this.tweet.content)
          this.tweet.content = convertEmoji(
            addColor(escape(this.tweet.content))
          );
        if (this.translation && this.translation.content)
          this.translation.content = convertEmoji(
            addColor(escape(this.translation.content))
          );
        this.user.display = convertEmoji(escape(this.user.display));

        this.dataReady = true;
      })
      .catch(error => {
        this.errorMessage = `请求Fridge失败：${error.message}`;
      });
  },
  methods: {
    formatDate(date) {
      var padZero = num => {
        if (num < 10) {
          return "0" + num;
        } else {
          return "" + num;
        }
      };
      var formatTimezone = offset => {
        offset = -offset;
        if (offset >= 0) {
          return "UTC+" + offset;
        } else {
          return "UTC" + offset;
        }
      };
      var d = new Date(date);
      return (
        `${d.getFullYear()} 年 ${d.getMonth()} 月 ${d.getDate()} 日 ` +
        `${padZero(d.getHours())}:${padZero(d.getMinutes())}:` +
        `${padZero(d.getSeconds())}` +
        `（${formatTimezone(d.getTimezoneOffset() / 60)}）`
      );
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="less">
.card {
  margin: 18px;
  margin-top: 16px;
  padding-bottom: 1px;
}

.cardTitle {
  height: 56px;
  padding-top: 18px;
  padding-bottom: 12px;

  .username {
    color: #888;
    font-size: 20px;
  }
  .avatar {
    float: left;
    height: 56px;
    border-radius: 28px;
    margin-right: 8px;
  }
  .displayName {
    padding-top: 2px;
    font-size: 22px;
    font-weight: bold;
    line-height: 24px;
    white-space: pre-wrap;
  }
}

.cardBody {
  white-space: pre-wrap;

  .mention,
  .hashtag,
  .link {
    color: #4daefd;
    display: inline-block;
  }
  .origText {
    legend {
      font-size: 18px;
      margin-bottom: 2px;
      color: #ff7d84;
    }
    background-color: rgba(128, 0, 0, 0.05);
    padding: 8px;
    margin-top: 14px;
    font-size: 18px;
  }
}

.media {
  margin-top: 22px;
  img {
    width: 100%;
    border-radius: 12px;
    margin-bottom: 12px;
  }
}

.postTime {
  color: #888;
  font-size: 14px;
}

.altBackground {
  background: #eee;
}
.normalBackground {
  background: #fff;
}
.tweet {
  border-radius: 16px;
}
</style>
