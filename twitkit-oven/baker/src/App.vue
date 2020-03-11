<template>
  <div>
    <Tweet
      v-if="ready"
      :tid="payloadData.tid"
      :status-id="statusId"
      :tweets="tweets"
      :trans-text="payloadData.transText"
    />
    <div v-html="errorMessage" v-if="errorMessage"></div>
  </div>
</template>

<script>
import Tweet from "./components/Tweet.vue";
import cookie from "cookie";
import axios from "axios";
import { v4 as uuidv4 } from "uuid";

export default {
  name: "App",
  components: {
    Tweet
  },
  data() {
    return {
      payloadData: null,
      errorMessage: null,
      tweetData: null,
      statusId: null,
      tweets: null,
      ready: false
    };
  },
  mounted() {
    var cookies = cookie.parse(document.cookie);
    try {
      this.payloadData = JSON.parse(cookies.payload_data);
    } catch (error) {
      this.errorMessage = "错误：" + error.message + "<br>";
      return;
    }
    if (!this.payloadData.tid && !this.payloadData.url) {
      this.errorMessage = "必须至少指定tid或url中的一个";
      return;
    } else if (this.payloadData.tid && this.payloadData.url) {
      this.errorMessage = "不能同时指定tid和url";
      return;
    }

    if (this.payloadData.url) {
      axios
        .post("/api_proxy/waitress/waitress/gettweet", {
          taskId: uuidv4(),
          forwardFrom: "twitkit-oven-baker",
          timestamp: new Date().toISOString(),
          data: { url: this.payloadData.url }
        })
        .then(response => {
          if (response.data.code != 0) {
            this.errorMessage = `Waitress返回${response.data.code}：${response.data.message}`;
          } else {
            this.tweets = response.data.tweets;
            this.statusId = response.data.rootId;
            this.ready = true;
          }
        })
        .catch(error => {
          this.errorMessage = `请求Waitress失败：${error.message}`;
        });
    } else {
      this.ready = true;
    }

    // CSS插入变量的hack
    var styleHtml = `
      <style>
        body {
          font-family: ${this.payloadData.zhFont};
        }
        *:lang(ja-jp) {
          font-family: ${this.payloadData.jaFont};
        }
      </style>`;
    document.head.innerHTML += styleHtml;
  }
};
</script>

<style lang="less">
body {
  padding: 0;
  color: #444;
  font-size: 22px;
}
</style>
