<template>
  <div>
    <Tweet :tid="tid" v-if="tid" />
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
      tid: null,
      errorMessage: null
    };
  },
  mounted() {
    var cookies = cookie.parse(document.cookie);
    try {
      var payload_data = JSON.parse(cookies.payload_data);
      this.tid = payload_data.tid;
    } catch (error) {
      this.errorMessage = "错误：" + error.message + "<br>";
    }
    if (!this.tid) {
      this.errorMessage += "读取tid失败";
    }
  }
};
</script>

<style lang="less">
body {
  font-family: "Source Han Sans SC";
  padding: 0;
  color: #444;
  font-size: 22px;
}

*:lang(ja-jp) {
  font-family: "Source Han Sans";
}
</style>
