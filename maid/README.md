# TwitKit-Maid

获取推特及Bilibili更新用的组件

## 环境要求

* Python 3.6 及以上
* requests

## 使用方法

1. 安装Python依赖：

   ```
   pip3 install -r requirements.txt
   ```

2. 执行`./configen.sh`创建配置文件（`config.py`），将必要的信息填入（具体下述）

3. 执行`./start.sh`启动Maid

   

### 配置文件选项说明

其他组件API设置：

* `FRIDGE_API_BASE`：Fridge API根URL
* `APP_API_BASE`：App API根URL
* `OVEN_API_BASE`：Oven API根URL

监听设置：

* `API_SERVER_HOST`：监听地址
* `API_SERVER_PORT`：监听端口

推特API相关设置：

* `CONSUMER_KEY`：Consumer Key
* `CONSUMER_SECRET`：Consumer Secret
* `ACCESS_TOKEN_KEY`：Access Token Key
* `ACCESS_TOKEN_SECRET`：Access Token Secret

RSSHub相关设置：

* `BILIBILI_UID`：监听的Bilibili账号的UID（考虑改为数据库中的设置）,设`None`可以禁用
* `UPDATE_INTERVAL`：更新间隔（秒）

日志设置：

* `LOG_DEBUG`：是否使用调试日志级别
* `LOG_FILE`：日志文件路径（`None`为不保存日志文件）

推送过滤设置：

* `PUSH_RETWEETS`：是否推送转推（布尔值）
* `PUSH_REPLIES`：是否推送回复（布尔值）

其他：

* `MAX_HISTORY_TWEETS`：启动时获取的最大历史推数（数据库中最新推开始至当前）

## API文档

### 从URL入库一条推

接口地址：`/api/maid/addtask`

请求和返回报文均为标准JSON格式

提交方法：POST

#### 请求报文

请求报文样例：

```
{
  "forwardFrom": ...,
  "timestamp": ...,
  "taskId": ...,
  "data": {
    "url": "https://twitter.com/magireco/status/1233776691064868865"
  }
}
```
（注：更正了`taskId`的位置）

参数说明：

* `url`：推文的URL

#### 返回报文

若请求成功，返回报文内容例：

```
{
  "code": 0,
  "message": "OK",
  "addedTid": [
    1001,
    1002,
    ...
  ],
  "rootTid": 1001
}
```

参数说明：

* `addedTid`：含有已入库的tid的数组
* `rootTid`：请求插入的推文本身的tid

（`addedTid`中`rootTid`之外的结果是由嵌套产生的）

目前可能出现的返回码：

* `0`：加入任务成功
* `400`：输入格式验证失败
* `500`： 推特API请求失败
* `501`： Fridge API请求失败

### 从URL获取一条推（不入库）

接口地址：`/api/maid/gettweet`

请求和返回报文均为标准JSON格式

提交方法：POST

#### 请求报文

请求报文样例：

```
{
  "forwardFrom": ...,
  "timestamp": ...,
  "taskId": ...,
  "data": {
    "url": "https://twitter.com/magireco/status/1233776691064868865"
  }
}
```

参数说明：

- `url`：推文的URL

#### 返回报文

若请求成功，返回报文内容例：

```
{
  "code": 0,
  "message": "OK",
  "tweets": {
    "1237693889504358401": {
      "twitter": {
        "statusId": "1237693889504358401",
        "url": "https://twitter.com/magireco/status/1237693889504358401",
        "content": "内容",
        "media": "[\"https://pbs.twimg.com/media/ES0r4KLUMAE-Ywe.jpg\"]",
        "refStatusId": null,
        "twitterUid": "761743038800474112",
        "pubDate": "2020-03-11T19:56:33+09:00",
      },
      "user": {
        "twitterUid": "761743038800474112",
        "name": "magireco",
        "display": "マギアレコード公式",
        "avatar": "https://pbs.twimg.com/profile_images/1205868817336766465/gqo0RuBq_400x400.jpg"
      }
    },
    ...
  },
  "rootStatusId": 1001
}
```

参数说明：

- `tweets`：得到的推（key是推特的Status ID，value是具体推文数据，格式和Fridge返回的类似）
  - `twitter.refStatus`：引用的Status ID（引用也一定在返回结果中）
  - 其余项意义和格式都和Fridge的一样
- `rootStatusId`：请求插入的推文本身的tid

（`tweets`中`rootStatusId`之外的结果是由嵌套产生的）

目前可能出现的返回码：

- `0`：获取推文成功
- `400`：输入格式验证失败
- `500`： 推特API请求失败