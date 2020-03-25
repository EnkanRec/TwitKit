module.exports = {
  logLevel: 3,
  type: "ws",
  server: "ws://localhost:6700",
  selfId: undefined,
  nickname: '',
  commandPrefix: '#',
  plugins: [
    ["common", {
      admin: false,
      broadcast: false,
      contextify: false,
      echo: false,
      exec: false,
      exit: false,
      info: false,
      help: true
    }],
    [".", {
      prefix: '#',      // 快捷指令前缀
      ispro: false,     // 是否发图
      cmd: {
        host: {
          store: "http://localhost:8220",
          translator: "http://localhost:8221",
          maid: "http://localhost:8222"
        },
        group: [],      // 监听命令的群组，留空监听所有人
        private: false, // 是否允许私聊上班
        friend: false,   // 是否允许好友上班
        cut: 8          // 消息预览截断长度
      },
      watcher: {
        port: 8223,     // 监听端口
        target: {       // 更新推送目标
          discuss: [],
          private: [],
          group: []
        }
      }
    }],
  ]
}