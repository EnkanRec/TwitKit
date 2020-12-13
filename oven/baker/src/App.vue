<template>
  <div id="app">
    <div v-if="isBakeRequest()">
      <BakeTweet />
    </div>
    <v-app v-else>
      <v-content>
        <div style="width:480px">
          <v-switch v-model="previewMode" label="预览模式"></v-switch>

          <Tweet
            class="tweet"
            v-if="selectedTask !== null && selectedTask >= 0"
            :statusId="tidOrder[selectedTask]"
            :tweets="tasks"
            :key="tidOrder[selectedTask]"
            :isWebTrans="!previewMode"
          ></Tweet>
        </div>
      </v-content>

      <v-navigation-drawer app>
        <!-- 左边栏列表 -->
        <v-list three-line>
          <v-subheader>任务列表</v-subheader>
          <v-list-item-group v-model="selectedTask" color="primary">
            <v-list-item v-for="(tid, i) in tidOrder" :key="i">
              <v-list-item-avatar>
                <v-img :src="tasks[tid].user.avatar"></v-img>
              </v-list-item-avatar>
              <v-list-item-content>
                <v-list-item-title v-html="`#${tid} (@${tasks[tid].user.name})`"></v-list-item-title>
                <v-list-item-subtitle v-html="tasks[tid].twitter.content"></v-list-item-subtitle>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-navigation-drawer>

      <v-snackbar v-model="showErrorMessage" top color="error">
        {{ errorMessage }}
        <v-btn text @click="showErrorMessage = false">关闭</v-btn>
      </v-snackbar>
    </v-app>
  </div>
</template>

<script>
import Tweet from "./components/Tweet";
import BakeTweet from "./views/BakeTweet";
import cookie from "cookie";
import { v4 as uuidv4 } from "uuid";
import axios from "axios";

export default {
  name: "App",

  components: {
    BakeTweet,
    Tweet
  },

  data() {
    return {
      tasks: null,
      selectedTask: null,
      errorMessage: null,
      tidOrder: null,
      previewMode: false,
      showErrorMessage: false
    };
  },

  mounted() {
    this.requestApi("/fridge/db/task/list", { tid: 0 }).then(respData => {
      this.tasks = {};
      this.tidOrder = [];
      for (var task of respData) {
        task.twitter.tid = task.twitter.tid.toString();
        if (task.twitter.refTid)
          task.twitter.refTid = task.twitter.refTid.toString();
        task.twitter.refStatusId = task.twitter.refTid;
        this.tidOrder.push(task.twitter.tid);
        this.tasks[task.twitter.tid] = task;
      }
    });
  },

  methods: {
    isBakeRequest() {
      var cookies = cookie.parse(document.cookie);
      return !!cookies.payload_data;
    },
    async requestApi(path, data) {
      var requestPayload = {
        forwardFrom: "twitkit-webbaker",
        timestamp: new Date().toISOString(),
        taskId: uuidv4(),
        data: data
      };

      try {
        const response = await axios.post("/api_proxy" + path, requestPayload);
        if (response.data.code != 0) {
          this.errorMessage = `${path}返回${response.data.code}：${response.data
            .msg || response.data.message}`;
          this.showErrorMessage = true;
        } else {
          return response.data.data;
        }
      } catch (e) {
        this.errorMessage = `请求${path}失败：${e.message}`;
        this.showErrorMessage = true;
      }
    }
  }
};
</script>

<style lang="less">
</style>