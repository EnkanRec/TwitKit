# TwitKit

[README](README.md) | [中文文档](README_zh.md)

TwitKit是一个面向推文翻译工作的开源套件。基于微服务架构设计，组件间均使用REST API通信。

### 主要贡献者

[k9yyy](https://github.com/k9yyy)：oven、maid组件

[Rinka](https://github.com/rinkako)：fridge组件

[MisakaHonoka](https://github.com/y2361547758)：koishi-app组件

[Inactive Virus](https://github.com/inactive-virus)：项目管理和shell scripts

## 后端组件

### fridge

数据库微服务，提供推文内容与元信息的存储服务。
现支持mysql/mariadb存储后端。

### oven

图像处理微服务，提供文字→图像的转换，同时附加可识别元素到图像中，供检测发布使用。

### maid

更新检测微服务，通过Twitter API检测推文更新；
同时提供检测Bilibili动态是否发布了成品图的功能。

## 前端组件

### koishi-app

QQBot前端，基于[koishi框架](https://koishi.js.org)实现与[酷Q机器人](https://cqp.cc/)的通信，提供QQ上的更新通知和译文提交、图片返回服务。

## 安装

请参阅各子文件夹的`README.md`；仓库中提供了用于加快部署速度的shell script，若需使用，请为相应用户赋予sudo权限，并建议在部署完成后撤销。
