# twitkit-fridge接口文档


### 接口：设置表set

- 接口功能
	
+ 接收来自http的请求，把某些设置更新入库
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/kv/set
	+ Request [POST]
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，key是要更新的设置表key，value是要更新的内容|
  |taskId|string|Y|Request ID|

   + Response

  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|string|总是一个空串|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {
	    "iroha.wife.name": "yachiyo tamaki"
	  }
	}
	```
	
	+ Response
	
	```json
	{
	  "code": 0,
	  "msg": "",
	  "data": ""
	}
	```

### 接口：设置表get

- 接口功能
	
+ 接收来自http的请求，把某些设置从库中获取
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/kv/get
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|list|Y|一个列表，代表要查询的key|
|taskId|string|Y|Request ID|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|一个字典，key是要查询的key，value是该key在数据库设置表里的value值，如果该key未曾出现在表里，value值为null|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": ["iroha.wife.name", "sana.wife.name"]
	}
	```
	
	+ Response
	
	```json
	{
	  "code": 0,
	  "msg": "",
	  "data": {
	    "iroha.wife.name": "yachiyo tamaki",
	    "sana.wife.name": null
	  }
	}
	```


### 接口：翻译任务获取单条

- 接口功能
	
+ 接收来自http的请求，获取一条推文任务和她的最新一条翻译
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/get
	+ Request [POST]
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|如果tid在库中无法找到推文，则为**null**。如果找得到，则为一个字典，一共有三个key：`twitter`和`translation`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":3}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:47:11.519+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T12:08:40.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        },
	        "translation": null
	    }
	}
	```

### 接口：翻译任务获取从给定tid起的多条

- 接口功能
	
+ 接收来自http的请求，获取tid大于给定数值的所有推文任务和她们的最新翻译
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/list
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid，大于此tid推文们会被检索|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|list|一个列表，如果给定tid在库中没有任何推文的tid比这更大，则为**空列表**。如果找得到，则是一个列表，列表中的元素是字典，这个字典一共有三个key：`twitter`和`translation`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":3}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:48:25.365+08:00",
	    "data": [
	        {
	            "twitter": {
	                "tid": 5,
	                "statusId": "200000000000",
	                "url": "",
	                "content": "转发的",
	                "media": "[]",
	                "published": false,
	                "hided": false,
	                "comment": "",
	                "twitterUid": "123123123123",
	                "refTid": 3,
	                "pubDate": "2020-02-29T14:40:00.000+0800",
	                "extra": "{\"aa\": 123123}",
	                "newdate": "2020-03-01T12:14:01.000+0800",
	                "updatetime": "2020-03-01T12:14:01.000+0800"
	            },
	            "user": {
	                "uid": 4,
	                "twitterUid": "123123123123",
	                "name": "enkanRecGLZ",
	                "display": "圆环纪录攻略组",
	                "avatar": "http://111/111.jpg",
	                "updatetime": "2020-03-01T12:08:29.000+0800",
	                "newdate": "2020-03-01T12:08:29.000+0800"
	            },
	            "translation": null
	        },
	        {
	            "twitter": {
	                "tid": 7,
	                "statusId": "200000000001",
	                "url": "",
	                "content": "转发的2",
	                "media": "[]",
	                "published": false,
	                "hided": false,
	                "comment": "",
	                "twitterUid": "123123123666",
	                "refTid": 3,
	                "pubDate": "2020-02-29T14:40:00.000+0800",
	                "extra": "{\"aa\": 123123}",
	                "newdate": "2020-03-01T12:14:52.000+0800",
	                "updatetime": "2020-03-01T12:14:52.000+0800"
	            },
	            "user": {
	                "uid": 5,
	                "twitterUid": "123123123666",
	                "name": "enkanRecGLZ_22",
	                "display": "圆环纪录攻略组_22",
	                "avatar": "http://111/111.jpg",
	                "updatetime": "2020-03-01T12:14:51.000+0800",
	                "newdate": "2020-03-01T12:14:51.000+0800"
	            },
	            "translation": null
	        }
	    ]
	}
	```

### 接口：为推文进行备注

- 接口功能
	
+ 接收来自http的请求，为一条推文添加备注信息
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/comment
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |tid|int|Y|整型数，推文的tid|
|comment|string|Y|备注内容|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|回显更新完毕的推文包装，该包装是一个字典，包含2个键值对：`twitter`为入库后的推文数据结构，如果tid在库中无法找到推文则为**null**，如果找得到则是对应原推文入库后的数据结构；`user`为发表此推文的关联账号的入库后数据结构，如果`twitter`为**null**则此字段总为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {
	    "tid":3,
	    "comment":"二叶纱奈"
	  }
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:55:50.874+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "二叶纱奈",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T12:08:40.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        }
	    }
	}
	```


### 接口：设置推文为隐藏

- 接口功能
	
+ 接收来自http的请求，将推文设置为隐藏，不会在`/list`接口中返回，也不会被`/last`接口认为是最后一条，但**不影响**`/actuallast`接口的返回
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/hide
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|回显更新完毕的推文包装，该包装是一个字典，包含2个键值对：`twitter`为入库后的推文数据结构，如果tid在库中无法找到推文则为**null**，如果找得到则是对应原推文入库后的数据结构；`user`为发表此推文的关联账号的入库后数据结构，如果`twitter`为**null**则此字段总为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":3}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:56:56.147+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": true,
	            "comment": "二叶纱奈",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T14:55:50.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        }
	    }
	}
	```


### 接口：设置推文为非隐藏

- 接口功能
	
+ 接收来自http的请求，将推文设置为非隐藏。
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/visible
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|回显更新完毕的推文包装，该包装是一个字典，包含2个键值对：`twitter`为入库后的推文数据结构，如果tid在库中无法找到推文则为**null**，如果找得到则是对应原推文入库后的数据结构；`user`为发表此推文的关联账号的入库后数据结构，如果`twitter`为**null**则此字段总为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":3}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:57:17.209+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "二叶纱奈",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T14:56:56.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        }
	    }
	}
	```


### 接口：设置推文为已发布

- 接口功能
	
+ 接收来自http的请求，将推文设置为已经发布完毕
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/published
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|回显更新完毕的推文包装，该包装是一个字典，包含2个键值对：`twitter`为入库后的推文数据结构，如果tid在库中无法找到推文则为**null**，如果找得到则是对应原推文入库后的数据结构；`user`为发表此推文的关联账号的入库后数据结构，如果`twitter`为**null**则此字段总为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":3}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:57:40.406+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": true,
	            "hided": false,
	            "comment": "二叶纱奈",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T14:57:17.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        }
	    }
	}
	```

### 接口：设置推文为未发布

- 接口功能
	
+ 接收来自http的请求，将推文设置为尚为发布（入库推文默认是未发布状态，此操作一般用来回滚发布动作）
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/unpublished
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|回显更新完毕的推文包装，该包装是一个字典，包含2个键值对：`twitter`为入库后的推文数据结构，如果tid在库中无法找到推文则为**null**，如果找得到则是对应原推文入库后的数据结构；`user`为发表此推文的关联账号的入库后数据结构，如果`twitter`为**null**则此字段总为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":3}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:58:13.191+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "二叶纱奈",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T14:57:40.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        }
	    }
	}
	```


### 接口：拉取最后一条推文（和翻译）（忽略隐藏）

- 接口功能
	
+ 接收来自http的请求，获取最后一条非隐藏推文。
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/last
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|N|一个字典，包含请求参数，非必要，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|withTranslation|bool|N|传`true`时，顺便返回推文的最新翻译；传`false`或不传时只返回推文，返回结构见案例|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|最后一条非隐藏的推文。如果无法找到任何推文，则为**null**。如果找得到且不需要翻译，则返回对应原推文入库后的数据结构；如果找得到且需要翻译，则返回一个字典，这个字典一共有三个key：`twitter`和`translation`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00"
	}
	```
	
	+ Response（不带翻译）
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T14:58:49.039+08:00",
	    "data": {
	        "twitter": {
	            "tid": 7,
	            "statusId": "200000000001",
	            "url": "",
	            "content": "转发的2",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123666",
	            "refTid": 3,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:14:52.000+0800",
	            "updatetime": "2020-03-01T12:14:52.000+0800"
	        },
	        "user": {
	            "uid": 5,
	            "twitterUid": "123123123666",
	            "name": "enkanRecGLZ_22",
	            "display": "圆环纪录攻略组_22",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:14:51.000+0800",
	            "newdate": "2020-03-01T12:14:51.000+0800"
	        }
	    }
	}
	```
	
	+ Response（带翻译）
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T15:00:01.052+08:00",
	    "data": {
	        "twitter": {
	            "tid": 7,
	            "statusId": "200000000001",
	            "url": "",
	            "content": "转发的2",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123666",
	            "refTid": 3,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:14:52.000+0800",
	            "updatetime": "2020-03-01T12:14:52.000+0800"
	        },
	        "user": {
	            "uid": 5,
	            "twitterUid": "123123123666",
	            "name": "enkanRecGLZ_22",
	            "display": "圆环纪录攻略组_22",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:14:51.000+0800",
	            "newdate": "2020-03-01T12:14:51.000+0800"
	        },
	        "translation": {
	            "zzid": 1,
	            "version": 0,
	            "translation": "翻译内容7",
	            "img": "http://f.jpg",
	            "newdate": "2020-03-01T14:59:58.000+0800",
	            "updatetime": "2020-03-01T14:59:58.000+0800"
	        }
	    }
	}
	```

### 接口：拉取最后一条推文（包含隐藏，即tid最大推文）

- 接口功能
	
+ 接收来自http的请求，获取最后一条（tid最大）推文。
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/actuallast
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
|taskId|string|Y|Request ID|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|最后一条推文的包装，该包装是一个字典，包含2个键值对：`twitter`为入库后的推文数据结构，如果tid在库中无法找到推文则为**null**，如果找得到则是对应原推文入库后的数据结构；`user`为发表此推文的关联账号的入库后数据结构，如果`twitter`为**null**则此字段总为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00"
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T15:01:09.235+08:00",
	    "data": {
	        "twitter": {
	            "tid": 7,
	            "statusId": "200000000001",
	            "url": "",
	            "content": "转发的2",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123666",
	            "refTid": 3,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:14:52.000+0800",
	            "updatetime": "2020-03-01T12:14:52.000+0800"
	        },
	        "user": {
	            "uid": 5,
	            "twitterUid": "123123123666",
	            "name": "enkanRecGLZ_22",
	            "display": "圆环纪录攻略组_22",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:14:51.000+0800",
	            "newdate": "2020-03-01T12:14:51.000+0800"
	        }
	    }
	}
	```


### 接口：入库单条推文

- 接口功能
	
+ 接收来自http的请求，将一条推文入库
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/create
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |url|string|Y|推文完整URL|
  |content|string|Y|推文内容|
|media|string|Y|推文的media信息|
  |pub_date|string|Y|推文的发表时间戳，，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
|status_id|string|Y|推文的唯一id号，字符串形式|
  |user_twitter_uid|string|Y|推文发表者的唯一id号，字符串形式|
  |user_name|string|Y|推文发表者的名字|
  |user_display|string|Y|推文发表者的显示用名字|
  |user_avatar|string|Y|推文发表者的头像描述子|
  |extra|string|N|推文附加字段，应用自行维护此字段的用途|
  |ref|string|N|推文的关联推文id（即查询推文接口得到的返回中的`tid`）|
  
   + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|一个字典，一共有三个key：`twitter`和`alreadyExist`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`alreadyExist`的键值是一个布尔值，代表在这次插入请求之前对应相同URL的推文是否已经入库过了；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-test",
	  "timestamp": "2020-02-29T14:40:00.000+08:00",
	  "data": {
	    "url": "URL_10",
	    "content": "嘤嘤嘤",
	    "media": "[]",
	    "pub_date": "2020-02-29T14:40:00.000+08:00",
	    "extra": "{\"aa\": 123123}",
	    "status_id": "100000000000",
	    "user_twitter_uid": "123123123123",
	    "user_name": "enkanRecGLZ",
	    "user_display": "圆环纪录攻略组",
	    "user_avatar": "http://111/111.jpg"
	  }
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T12:13:10.171+08:00",
	    "data": {
	        "twitter": {
	            "tid": 3,
	            "statusId": "100000000000",
	            "url": "URL_10",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123123",
	            "refTid": null,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:08:40.000+0800",
	            "updatetime": "2020-03-01T12:08:40.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        },
	        "alreadyExist": true
	    }
	}
	```


### 接口：批量入库推文

- 接口功能
	
+ 接收来自http的请求，将给定的推文集全部入库
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/bulk
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|list|Y|一个列表，列表中的每个元素是一个字典，包含请求参数，参数见《Request.data[列表元素]》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data[列表元素]
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |url|string|Y|推文完整URL|
  |content|string|Y|推文内容|
|media|string|Y|推文的media信息|
  |pub_date|string|Y|推文的发表时间戳，，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
|status_id|string|Y|推文的唯一id号，字符串形式|
  |user_twitter_uid|string|Y|推文发表者的唯一id号，字符串形式|
  |user_name|string|Y|推文发表者的名字|
  |user_display|string|Y|推文发表者的显示用名字|
  |user_avatar|string|Y|推文发表者的头像描述子|
  |extra|string|N|推文附加字段，应用自行维护此字段的用途|
  |ref|string|N|推文的关联推文id（即查询推文接口得到的返回中的`tid`）|
  
   + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|一个列表，列表中的每个元素是一个字典。字典一共有三个key：`twitter`和`alreadyExist`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`alreadyExist`的键值是一个布尔值，代表在这次插入请求之前对应相同URL的推文是否已经入库过了；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": [
	    {
	      "url": "URL_10",
	      "content": "嘤嘤嘤",
	      "media": "[]",
	      "pub_date": "2020-02-29T14:40:00.000+08:00",
	      "extra": "{\"aa\": 123123}",
	      "status_id": "100000000000",
	      "user_twitter_uid": "123123123123",
	      "user_name": "enkanRecGLZ",
	      "user_display": "圆环纪录攻略组",
	      "user_avatar": "http://111/111.jpg"
	    },
	    {
	      "url": "URL_11",
	      "content": "嘤嘤嘤2",
	      "media": "[]",
	      "pub_date": "2020-02-29T14:40:00.000+08:00",
	      "extra": "{\"aa\": 555}",
	      "status_id": "100000000001",
	      "user_twitter_uid": "123123123123",
	      "user_name": "enkanRecGLZ",
	      "user_display": "圆环纪录攻略组",
	      "user_avatar": "http://111/111.jpg"
	    }
	  ]
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T15:11:35.645+08:00",
	    "data": [
	        {
	            "twitter": {
	                "tid": 3,
	                "statusId": "100000000000",
	                "url": "URL_10",
	                "content": "嘤嘤嘤",
	                "media": "[]",
	                "published": false,
	                "hided": false,
	                "comment": "二叶纱奈",
	                "twitterUid": "123123123123",
	                "refTid": null,
	                "pubDate": "2020-02-29T14:40:00.000+0800",
	                "extra": "{\"aa\": 123123}",
	                "newdate": "2020-03-01T12:08:40.000+0800",
	                "updatetime": "2020-03-01T14:58:13.000+0800"
	            },
	            "user": {
	                "uid": 4,
	                "twitterUid": "123123123123",
	                "name": "enkanRecGLZ",
	                "display": "圆环纪录攻略组",
	                "avatar": "http://111/111.jpg",
	                "updatetime": "2020-03-01T12:08:29.000+0800",
	                "newdate": "2020-03-01T12:08:29.000+0800"
	            },
	            "alreadyExist": true
	        },
	        {
	            "twitter": {
	                "tid": 8,
	                "statusId": "100000000001",
	                "url": "URL_11",
	                "content": "嘤嘤嘤2",
	                "media": "[]",
	                "published": false,
	                "hided": false,
	                "comment": "",
	                "twitterUid": "123123123123",
	                "refTid": null,
	                "pubDate": "2020-02-29T14:40:00.000+0800",
	                "extra": "{\"aa\": 555}",
	                "newdate": "2020-03-01T15:11:35.000+0800",
	                "updatetime": "2020-03-01T15:11:35.000+0800"
	            },
	            "user": {
	                "uid": 4,
	                "twitterUid": "123123123123",
	                "name": "enkanRecGLZ",
	                "display": "圆环纪录攻略组",
	                "avatar": "http://111/111.jpg",
	                "updatetime": "2020-03-01T12:08:29.000+0800",
	                "newdate": "2020-03-01T12:08:29.000+0800"
	            },
	            "alreadyExist": false
	        }
	    ]
	}
	```


### 接口：删除入库推文

- 接口功能
	
+ 接收来自http的请求，将某条已入库推文删除，这个操作不能回滚
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/delete
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|bool|如果在数据库中发生了删除动作返回`true`，否则返回`false`|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid": 1013}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-06T17:23:15.792+08:00",
	    "data": true
	}
	```


### 接口：为推文新增一个翻译

- 接口功能
	
+ 接收来自http的请求，翻译一条推文，产生一个翻译版本
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/translate
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |tid|int|Y|要翻译的推文tid|
  |img|string|Y|烤推完毕的图片地址|
|trans|string|Y|译文内容|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|如果tid在库中无法找到推文，则为**null**。如果找得到，则为一个字典，一共有三个key：`twitter`和`translation`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {
	    "tid": 5,
	    "img": "[]",
	    "trans": "翻译AA4"
	  }
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T15:12:09.005+08:00",
	    "data": {
	        "twitter": {
	            "tid": 5,
	            "statusId": "200000000000",
	            "url": "",
	            "content": "转发的",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123123",
	            "refTid": 3,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:14:01.000+0800",
	            "updatetime": "2020-03-01T12:14:01.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        },
	        "translation": {
	            "zzid": 2,
	            "version": 0,
	            "translation": "翻译AA4",
	            "img": "[]",
	            "newdate": "2020-03-01T15:12:08.000+0800",
	            "updatetime": "2020-03-01T15:12:08.000+0800"
	        }
	    }
	}
	```

### 接口：回滚一个推文的翻译版本

- 接口功能
	
+ 接收来自http的请求，将某条推文的翻译向前回滚一个版本
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/rollback
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|如果tid在库中无法找到推文，则为**null**。如果找得到，则为一个字典，一共有三个key：`twitter`和`translation`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应回滚之后的最新的一条翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid": 5}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T15:13:03.426+08:00",
	    "data": {
	        "twitter": {
	            "tid": 5,
	            "statusId": "200000000000",
	            "url": "",
	            "content": "转发的",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123123",
	            "refTid": 3,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:14:01.000+0800",
	            "updatetime": "2020-03-01T12:14:01.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        },
	        "translation": null
	    }
	}
	```

### 接口：列出一个推文的全部翻译版本

- 接口功能
	
+ 接收来自http的请求，获取一条推文的全部翻译
	
- 接口协议
	+ 接口形式: REST API
	+ 接口地址: /api/db/task/translations
	+ Request [POST]
	
	|名称|格式|必传|备注|
  |:--|:--|:--|:--|
  |forwardFrom|string|Y|一个字符串，标定是哪个服务调用了此接口，如`twitkit-app`|
  |timestamp|string|Y|请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00`|
  |data|dict|Y|一个字典，包含请求参数，参数见《Request.data》小节|
|taskId|string|Y|Request ID|
  
	 + Request.data
	
  |名称|格式|必传|备注|
  |:--|:--|:--|:--|
|tid|int|Y|整型数，推文的tid|
  
 + Response
  
  |名称|格式|备注|
  |:--|:--|:--|
  |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
  |msg|string|备注信息，用来储存异常信息等|
  |data|dict|一个字典，一共有三个key：`twitter`和`translations`和`user`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构，如果tid在库中无法找到推文，则为**null**。`translations`的键值是一个列表，列表中每个元素是一个翻译内容入库后的数据结构，列表按照翻译版本字段`version`降序排序；如果tid在库中无法找到推文，则为**null**；`user`是发表此推特的关联账号入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "taskId": "bf5de87e-755d-4286-9cd4-3bdbc6583a34",
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid": 5}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-03-01T15:13:41.562+08:00",
	    "data": {
	        "twitter": {
	            "tid": 5,
	            "statusId": "200000000000",
	            "url": "",
	            "content": "转发的",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "twitterUid": "123123123123",
	            "refTid": 3,
	            "pubDate": "2020-02-29T14:40:00.000+0800",
	            "extra": "{\"aa\": 123123}",
	            "newdate": "2020-03-01T12:14:01.000+0800",
	            "updatetime": "2020-03-01T12:14:01.000+0800"
	        },
	        "user": {
	            "uid": 4,
	            "twitterUid": "123123123123",
	            "name": "enkanRecGLZ",
	            "display": "圆环纪录攻略组",
	            "avatar": "http://111/111.jpg",
	            "updatetime": "2020-03-01T12:08:29.000+0800",
	            "newdate": "2020-03-01T12:08:29.000+0800"
	        },
	        "translations": [
	            {
	                "zzid": 4,
	                "version": 1,
	                "translation": "翻译版本2",
	                "img": "[]",
                "newdate": "2020-03-01T15:13:32.000+0800",
	                "updatetime": "2020-03-01T15:13:32.000+0800"
	            },
	            {
	                "zzid": 3,
	                "version": 0,
	                "translation": "翻译版本1",
	                "img": "[]",
	                "newdate": "2020-03-01T15:13:28.000+0800",
	                "updatetime": "2020-03-01T15:13:28.000+0800"
	            }
	        ]
	    }
	}
	```
	
	