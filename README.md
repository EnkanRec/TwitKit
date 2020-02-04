# koishi-plugin-TwitKit

一个基于Koishi的推文翻译工作流开源实现

使用混合语言开发，组件间全部采用REST API通信

## 组件说明

### twitkit-app

基于koishi框架的APP部分，业务逻辑依赖于其他组件

### twitkit-stargazer

依赖RSSHub（暂定）的更新检测组

### twitkit-fridge

封装成微服务的数据库组件

### twitkit-cooker

基于wkhtmltoimage的烤图组件