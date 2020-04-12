<template>
  <div
    class="tweet"
    :class="`${altBg ? 'altBackground' : 'normalBackground'} ${inline && 'inline'}`"
  >
    <div class="refTweet" v-if="isReply && (tweet.refTid || tweet.refStatusId)">
      <Tweet
        :tid="tweets && tweet.refStatusId in tweets ? undefined : tweet.refTid"
        :tweets="tweets"
        :statusId="tweet.refStatusId"
        :alt-bg="altBg"
        :no-date="true"
        :isWebTrans="isWebTrans"
        :inline="true"
      />
    </div>
    <div class="card" v-if="dataReady">
      <div class="cardTitle">
        <img class="avatar" :src="user.avatar" />
        <div class="userInfo">
          <div class="displayName" lang="ja-jp" v-html="formattedUserDispName"></div>
          <div class="username">@{{ user.name }}</div>
        </div>
      </div>
      <div class="replyDecorationLine" :style="`height: ${decorationLineHeight}px`" v-if="inline"></div>
      <div class="cardBody" ref="cardBody">
        <div v-if="isWebTrans">
          <TranslationBox :origTransText="plainTransText"></TranslationBox>
        </div>
        <div
          v-html="formattedTransText || formattedContent"
          :lang="formattedTransText ? null : 'ja-jp'"
        ></div>
        <div class="origText" v-if="formattedTransText && formattedContent">
          <legend>原文</legend>
          <div lang="ja-jp" v-html="formattedContent"></div>
        </div>

        <div class="media">
          <img v-for="url in mediaList" :key="url" :src="url" />
        </div>

        <div class="refTweet" v-if="!isReply && (tweet.refTid || tweet.refStatusId)">
          <Tweet
            :tid="tweets && tweet.refStatusId in tweets ? undefined : tweet.refTid"
            :tweets="tweets"
            :statusId="tweet.refStatusId"
            :alt-bg="!altBg"
            :no-date="true"
            :isWebTrans="isWebTrans"
          />
        </div>
      </div>
      <div class="postTime" v-if="!noDate">▶ 原推发表于 {{ formatDate(tweet.pubDate) }}</div>
    </div>
    <div v-html="errorMessage" v-if="errorMessage"></div>
  </div>
</template>

<script>
import axios from "axios";
import { parse as twimojiParse } from "twemoji-parser";
import escape from "escape-html";
import { v4 as uuidv4 } from "uuid";
import TranslationBox from "./TranslationBox";
import Vue from "vue";
import elementResizeEvent from "element-resize-event";

export default {
  name: "Tweet",
  components: {
    TranslationBox
  },
  props: {
    tid: String,
    statusId: String,
    url: String,
    altBg: Boolean,
    transText: String,
    noDate: Boolean,
    tweets: Object,
    isWebTrans: Boolean,
    inline: Boolean
  },
  data() {
    return {
      tweet: null,
      user: null,
      dataReady: false,
      plainTransText: null,
      formattedTransText: null,
      formattedContent: null,
      formattedUserDispName: null,
      errorMessage: null,
      isReply: false,
      mediaList: [],
      decorationLineHeight: 0
    };
  },
  mounted() {
    if (!this.tid && !this.url && !this.tweets) {
      this.errorMessage = "没有传入数据";
      return;
    }
    if (this.statusId) {
      this.renderTweet(this.tweets[this.statusId]);
    } else {
      if (this.url) {
        axios
          .post("/api_proxy/maid/maid/gettweet", {
            taskId: uuidv4(),
            forwardFrom: "twitkit-oven-baker",
            timestamp: new Date().toISOString(),
            data: { url: this.url }
          })
          .then(response => {
            if (response.data.code != 0) {
              this.errorMessage = `Maid返回${response.data.code}：${response.data.message}`;
            } else {
              this.tweets = response.data.tweets;
              this.statusId = response.data.rootId;
              this.renderTweet(this.tweets[this.statusId]);
              this.dataReady = true;
            }
          })
          .catch(error => {
            this.errorMessage = `请求Maid失败：${error.message}`;
          });
      } else if (this.tid) {
        axios
          .post("/api_proxy/fridge/db/task/get", {
            taskId: uuidv4(),
            forwardFrom: "twitkit-oven-baker",
            timestamp: new Date().toISOString(),
            data: { tid: this.tid }
          })
          .then(response => {
            if (response.data.code != 0) {
              this.errorMessage = `Fridge返回${response.data.code}：${response.data.message}`;
              return;
            }
            this.renderTweet(response.data.data);
          })
          .catch(error => {
            this.errorMessage = `请求Fridge失败：${error.message}`;
          });
      }
    }
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
      var formatted;

      // 实测中遇到了Date解析不出来ISO8601的格式的情况，所以加了下面的代码作为fallback
      if (isNaN(d.getFullYear())) {
        d.setUTCFullYear(parseInt(date.substring(0, 4)));
        d.setUTCMonth(parseInt(date.substring(5, 7)) - 1);
        d.setUTCDate(parseInt(date.substring(8, 10)));
        d.setUTCHours(parseInt(date.substring(11, 13)));
        d.setUTCMinutes(parseInt(date.substring(14, 16)));
        d.setUTCSeconds(parseInt(date.substring(17, 19)));

        date = date.substring(19);
        var tzPos = date.indexOf("+");
        if (tzPos == -1) tzPos = date.indexOf("-");
        if (tzPos != -1) {
          var tzStr = date.substring(tzPos).replace(":", "");
          var tzOffset = parseFloat(tzStr);
          if (Math.abs(tzOffset) > 12) tzOffset /= 100;
          tzOffset *= 60 * 60 * 1000;
          d.setTime(d.getTime() - tzOffset);
        }
      }

      formatted =
        `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日 ` +
        `${padZero(d.getHours())}:${padZero(d.getMinutes())}:` +
        `${padZero(d.getSeconds())}` +
        `（${formatTimezone(d.getTimezoneOffset() / 60)}）`;

      return formatted;
    },

    renderTweet(tweetData) {
      this.tweet = tweetData.twitter;
      this.user = tweetData.user;
      var translation = tweetData.translation;
      this.plainTransText =
        this.transText || (translation ? translation.translation : null);
      if (this.tweet === null) {
        this.errorMessage = "推文不存在";
        return;
      }
      if (this.tweet && this.tweet.content)
        this.formattedContent = this.formatContent(this.tweet.content);
      if (this.translation && this.translation.content)
        this.formattedContent = this.formatContent(this.translation.content);
      if (this.plainTransText)
        this.formattedTransText = this.formatContent(this.plainTransText);
      this.formattedUserDispName = this.formatName(this.user.display);

      this.mediaList = JSON.parse(this.tweet.media);

      // 临时的回复判断，目前推文类型没有入库
      if (this.tweet.content) {
        this.isReply =
          this.tweet.content[0] == "@" &&
          (this.tweet.refTid || this.tweet.refStatusId);
      }
      
      this.dataReady = true;

      var calcDecoLineHeight = () => {
        this.decorationLineHeight =
          this.$refs.cardBody.clientHeight + (!this.mediaList.length ? 22 : 2);
        // 回复画线的hack
      };
      Vue.nextTick(() => {
        calcDecoLineHeight();
        elementResizeEvent(this.$refs.cardBody, calcDecoLineHeight);
      });
    },

    formatContent(str) {
      return this.convertEmoji(this.addColor(escape(str)));
    },

    formatName(str) {
      return this.convertEmoji(this.addColor(escape(str)));
    },

    convertEmoji(str) {
      var entities = twimojiParse(str);
      for (var e of entities) {
        var emojiImg = `<img src="${e.url}" class="emoji">`;
        str = str.replace(e.text, emojiImg);
      }
      return str;
    },

    addColor(str) {
      str = str.replace(/(@\w{1,15})/g, '<span class="mention">$1</span>');
      str = str.replace(
        /(^|\s)(#[^\s,.!?，。！？<]*)/g,
        '$1<span class="hashtag">$2</span>'
      );
      var entities = this.tweet.extra ? JSON.parse(this.tweet.extra) : {};
      JSON.parse(this.tweet.extra);
      if (entities && entities.urls) {
        for (var url of entities.urls) {
          str = str.replace(
            url.url,
            `<span class="link">${url.display_url}</span>`
          );
        }
      }
      str = str.replace(
        /(https?:\/\/[\w-_~:[\]@!$&'()*+,;=.?%#/]+)/g,
        '<span class="link">$1</span>'
      );
      return str;
    }
  }
};
</script>

<style lang="less">
.card {
  margin: 18px;
  margin-top: 16px;
  padding-bottom: 1px;
  overflow-wrap: break-word;
}

.cardTitle {
  height: 56px;
  padding-top: 18px;
  padding-bottom: 12px;

  .inline & .userInfo {
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    margin-bottom: 8px;
  }

  .username,
  .displayName {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    .inline & {
      display: inline;
    }
  }

  .username {
    color: #888;
    font-size: 20px;
    .inline & {
      margin-left: 0.5em;
    }
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

  .inline & {
    margin-left: 62px;
    margin-top: -28px;
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

img.emoji {
  height: 1em;
  width: 1em;
  margin: 0 0.05em 0 0.1em;
  vertical-align: -0.1em;
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

.inline .replyDecorationLine {
  background: #4daefd5d;
  width: 3px;
  position: absolute;
  margin-left: 27px;
  margin-top: -2px;
  border-radius: 2px;
}
</style>
