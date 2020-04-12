module.exports = {
  publicPath: "./",
  devServer: {
    proxy: {
      "/api_proxy": {
        target: "http://localhost:5000/"
      }
    }
  },
  transpileDependencies: ["vuetify"]
};
