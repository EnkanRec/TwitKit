# koishi-plugin-TwitKit

一个基于Koishi的推文翻译工作流开源实现

使用混合语言开发，组件间全部采用REST API通信

## 组件说明

### koishi-app

基于koishi框架的APP部分，业务逻辑依赖于其他组件

### maid

依赖推特API和RSSHub的更新检测组

### fridge

封装成微服务的数据库组件

### oven

基于wkhtmltoimage的烤图组件
