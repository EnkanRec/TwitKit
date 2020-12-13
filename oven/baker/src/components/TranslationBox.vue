<template>
  <v-card class="mb-4">
    <v-form ref="form" class="pa-0" v-model="valid">
      <label class="caption mx-4" v-if="origTransText != transText" :value="origTransText">
        <v-icon small>info</v-icon>修改尚未提交
      </label>
      <v-textarea
        class="mx-4"
        v-model="transText"
        label="译文"
        :rows="1"
        auto-grow
        single-line
        :rules="[v => !!v && !!v.trim() || '译文不能为空']"
        @input="refreshTweet"
      ></v-textarea>
      <v-row align="center" justify="center" class="ma-0 pa-0">
        <v-col class="pa-0">
          <v-btn
            color="primary"
            class="pa-0"
            depressed
            block
            tile
            :disabled="!valid || origTransText == transText"
            @click="submit"
          >提交</v-btn>
        </v-col>
        <v-col class="pa-0">
          <v-btn class="pa-0" depressed block tile @click="showHistory">查看历史</v-btn>
        </v-col>
      </v-row>
    </v-form>
  </v-card>
</template>

<script>
export default {
  props: {
    origTransText: String
  },
  data() {
    return {
      valid: false,
      transText: null
    };
  },
  mounted() {
    this.transText = this.origTransText;
  },
  methods: {
    refreshTweet() {
      this.$parent.formattedTransText = this.$parent.formatContent(
        this.transText
      );
    },
    submit() {
      // TODO
    }
  }
};
</script>

<style scoped>
</style>