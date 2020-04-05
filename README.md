# TwitKit

[README](README.md) | [中文文档](README_zh.md)

> WARNING: Machine-translation detected.

TwitKit is an open source suite for tweet translation. Based on microservices architecture design, components communicate with each other with REST API.

## backend components

### fridge
Database microservice which provides storage services for tweet content and metadata.
Mysql/mariadb storage backend is now supported.

### oven
Image processing microservice which provides text-to-image conversion while attaching recognizable elements to the image for publish detection.

### maid
Update detection microservice to detect tweet updates via the Twitter API.
Also provides the ability to detect whether Bilibili Dynamics has released an output image.

## front-end components

### koishi-app
QQBot front-end, based on [koishi](https://koishi.js.org) framework to communicate with CoolQ robot, provides QQ update notification, translation submission and image return services.

## install
See README.md in each subfolder; shell scripts are available in the repository for quick deployments. To use them, please give sudo privileges to the appropriate users and recommend revoking them after deployment is complete.
