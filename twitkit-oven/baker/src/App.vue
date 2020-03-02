<template>
  <div v-if="payloadData">
    <Tweet :tid="payloadData.tid" :trans-text="payloadData.transText" />
    <div v-html="errorMessage" v-if="errorMessage"></div>
  </div>
</template>

<script>
import Tweet from "./components/Tweet.vue";
import cookie from "cookie";

export default {
  name: "App",
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
      this.payloadData = JSON.parse(cookies.payload_data);
    } catch (error) {
      this.errorMessage = "错误：" + error.message + "<br>";
    }
    if (!this.payloadData.tid) {
      this.errorMessage += "读取tid失败";
    }

    // CSS插入变量的hack
    var styleHtml = `
      <style>
        body {
          font-family: "${this.payloadData.zhFont || "Source Han Sans SC"}";
        }
        *:lang(ja-jp) {
          font-family: "${this.payloadData.jaFont || "Source Han Sans"}";
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
