## WebSocket使用方式说明

#### 协议
使用SocketIO协议来完成通讯。建立连接后，请求和返回都通过长连接进行。

#### 请求
请求体是一个JSON，固定发送到事件`twitkit_request`，事件内容是一个JSON，结构：

|名称|格式|必传|备注|
|:--|:--|:--|:--|
|forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
|timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
|of|string|Y|服务类型，即要请求的服务在RestAPI清单里的URI的第**3**个部分|
|command|string|Y|服务URI，即要请求的服务在RestAPI清单里的URI的第**4**个部分|
|data|dict|Y|请求参数包，即要请求的服务在RestAPI清单里的**data**字段|

以`/api/db/task/get`为例，其请求体的一个例子是：

```json
{
  "forwardFrom": "twitkit-app",
  "timestamp": "2020-01-29T14:40:00.000+08:00",
  "of": "task",
  "command": "get",
  "data": {
    "tid": 1001
  }
}
```

此JSON作为事件体，通过SocketIO协议发送到DB模块，代码形如：`socketio.emit("twitkit_request", "请求体JSON所dump得到的字符串")`

#### 响应
响应体是一个JSON字符串，固定发送到事件`twitkit_response`，客户端通过订阅这个事件来监听返回。一个客户端只会收到属于它自己的返回，不会收到其它客户端的返回。

响应体结构和RestAPI的响应结构完成一致。