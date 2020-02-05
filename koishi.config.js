module.exports = {
  type: "ws",
  server: "ws://localhost:6700",
  selfId: 2663971093,
  nickname: '呐',
  commandPrefix: '#',
  plugins: [
    ["twitkit-app", {
      prefix: '#',// 快捷指令前缀
      ispro: false, // 是否发图（考虑ctx.sender.canSendImage）
      cut: 8,     // 消息预览截断长度
      port: 1551, // 监视器推送端口
      target: {   // 监视器更新推送目标
        discuss: [],
        private: [2361547758],
        group: []
      }
    }],
  ],
  // database: {
  //   mysql: {
  //     host: 'localhost',
  //     port: 3306,
  //     user: 'qqbot',
  //     password: 'qqbot',
  //     database: 'qqbot',
  //   },
  // },
}