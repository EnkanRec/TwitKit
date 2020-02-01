module.exports = {
  type: "ws",
  server: "ws://localhost:6700",
  selfId: 2663971093,
  nickname: '呐',
  commandPrefix: '#',
  plugins: [
    ["twitkit-app", {
      prefix: '#',
      ispro: false,
      cut: 8
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