<template>
  <div>
    <Tweet
      v-if="payloadData"
      :tid="payloadData.tid"
      :url="payloadData.url"
      :trans-text="payloadData.transText"
    />
    <div v-html="errorMessage" v-if="errorMessage"></div>
  </div>
</template>

<script>
import Tweet from "@/components/Tweet.vue";
import cookie from "cookie";

export default {
  name: "BakeTweet",
  components: {
    Tweet
  },
  data() {
    return {
      payloadData: null,
      errorMessage: null
    };
  },
  mounted() {
    var cookies = cookie.parse(document.cookie);
    try {
      this.payloadData = JSON.parse(decodeURIComponent(cookies.payload_data));
    } catch (error) {
      this.errorMessage = "错误：" + error.message + "<br>";
      return;
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
  margin: 8px;
}

*, ::before, ::after {
  box-sizing:content-box !important;
}
</style>
