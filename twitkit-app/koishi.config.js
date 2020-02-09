module.exports = {
  logLevel: 3,
  type: "ws",
  server: "ws://localhost:6700",
  selfId: 2663971093,
  nickname: '呐',
  commandPrefix: '#',
  plugins: [
    [".", {
      host: {
        translator: "http://localhost",
        store: "http://localhost"
      },
      prefix: '#',// 快捷指令前缀
      ispro: false, // 是否发图
      cut: 8,     // 消息预览截断长度
      port: 1551, // 监视器推送端口
      target: {   // 监视器更新推送目标
        discuss: [],
        private: [2361547758],
        group: []
      }
    }],
  ]
}