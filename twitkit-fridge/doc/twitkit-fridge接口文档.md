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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |tid|int|Y|整型数，推文的tid|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|如果tid在库中无法找到推文，则为**null**。如果找得到，则为一个字典，一共有两个key：`twitter`和`translation`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":1001}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-04T16:07:40.989+08:00",
	    "data": {
	        "twitter": {
	            "tid": 1001,
	            "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	            "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	            "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "newdate": "2020-02-04T05:15:36.000+0000",
	            "updatetime": "2020-02-04T05:15:36.000+0000"
	        },
	        "translation": {
	            "zzid": 3,
	            "version": 1,
	            "translation": "翻译222",
	            "img": "http://2",
	            "newdate": "2020-02-04T07:45:21.000+0000",
	            "updatetime": "2020-02-04T07:45:21.000+0000"
	        }
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |tid|int|Y|整型数，推文的tid，大于此tid推文们会被检索|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|list|一个列表，如果给定tid在库中没有任何推文的tid比这更大，则为**空列表**。如果找得到，则是一个列表，列表中的元素是字典，这个字典一共有两个key：`twitter`和`translation`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":1000}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-04T22:12:47.709+08:00",
	    "data": [
	        {
	            "twitter": {
	                "tid": 1001,
	                "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	                "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	                "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	                "published": false,
	                "hided": false,
	                "comment": "",
	                "newdate": "2020-02-04T13:15:36.000+0800",
	                "updatetime": "2020-02-04T13:15:36.000+0800"
	            },
	            "translation": {
	                "zzid": 3,
	                "version": 1,
	                "translation": "翻译222",
	                "img": "http://2",
	                "newdate": "2020-02-04T15:45:21.000+0800",
	                "updatetime": "2020-02-04T15:45:21.000+0800"
	            }
	        },
	        {
	            "twitter": {
	                "tid": 1002,
	                "url": "https://tamaki.com/iroha",
	                "content": "第二条含有emoji😁的推文",
	                "media": "[]",
	                "published": true,
	                "hided": false,
	                "comment": "小淋雨可爱",
	                "newdate": "2020-02-04T13:47:39.000+0800",
	                "updatetime": "2020-02-04T22:11:56.000+0800"
	            },
	            "translation": {
	                "zzid": 6,
	                "version": 2,
	                "translation": "BBB3",
	                "img": "b33",
	                "newdate": "2020-02-04T15:46:04.000+0800",
	                "updatetime": "2020-02-04T15:46:04.000+0800"
	            }
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
   |data|dict|回显更新完毕的推文。如果tid在库中无法找到推文，则为**null**。如果找得到则是对应原推文入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {
	    "tid":1001,
	    "comment":"小淋雨可爱"
	  }
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-04T23:32:24.312+08:00",
	    "data": {
	        "tid": 1001,
	        "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	        "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	        "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	        "published": false,
	        "hided": false,
	        "comment": "小淋雨可爱",
	        "newdate": "2020-02-04T13:15:36.000+0800",
	        "updatetime": "2020-02-04T13:15:36.000+0800"
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |tid|int|Y|整型数，推文的tid|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|回显更新完毕的推文。如果tid在库中无法找到推文，则为**null**。如果找得到则是对应原推文入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":1001}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-04T23:32:24.312+08:00",
	    "data": {
	        "tid": 1001,
	        "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	        "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	        "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	        "published": false,
	        "hided": true,
	        "comment": "小淋雨可爱",
	        "newdate": "2020-02-04T13:15:36.000+0800",
	        "updatetime": "2020-02-04T13:15:36.000+0800"
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |tid|int|Y|整型数，推文的tid|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|回显更新完毕的推文。如果tid在库中无法找到推文，则为**null**。如果找得到则是对应原推文入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid":1001}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-04T23:32:24.312+08:00",
	    "data": {
	        "tid": 1001,
	        "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	        "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	        "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	        "published": false,
	        "hided": false,
	        "comment": "小淋雨可爱",
	        "newdate": "2020-02-04T13:15:36.000+0800",
	        "updatetime": "2020-02-04T13:15:36.000+0800"
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |withTranslation|bool|N|传`true`时，顺便返回推文的最新翻译；传`false`或不传时只返回推文，返回结构见案例|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|最后一条非隐藏的推文。如果无法找到任何推文，则为**null**。如果找得到且不需要翻译，则返回对应原推文入库后的数据结构；如果找得到且需要翻译，则返回一个字典，一共有两个key：`twitter`和`translation`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00"
	}
	```
	
	+ Response（不带翻译）
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-05T11:33:32.956+08:00",
	    "data": {
	        "tid": 1001,
	        "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	        "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	        "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	        "published": false,
	        "hided": false,
	        "comment": "小淋雨可爱",
	        "newdate": "2020-02-04T13:15:36.000+0800",
	        "updatetime": "2020-02-05T00:01:20.000+0800"
	    }
	}
	```
	
	+ Response（带翻译）
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-05T17:58:22.458+08:00",
	    "data": {
	        "twitter": {
	            "tid": 1001,
	            "url": "https://gitlab.com/EnkanRec/koishi-plugin-twitkit/issues/1",
	            "content": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
	            "media": "[\"https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig\"]",
	            "published": false,
	            "hided": false,
	            "comment": "小淋雨可爱",
	            "newdate": "2020-02-04T13:15:36.000+0800",
	            "updatetime": "2020-02-05T17:40:17.000+0800"
	        },
	        "translation": {
	            "zzid": 23,
	            "version": 0,
	            "translation": "翻译啊",
	            "img": "[\"img_src\"]",
	            "newdate": "2020-02-05T17:39:13.000+0800",
	            "updatetime": "2020-02-05T17:39:13.000+0800"
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

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|最后一条推文。如果无法找到任何推文，则为**null**。如果找得到则是对应原推文入库后的数据结构|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00"
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-05T11:33:35.937+08:00",
	    "data": {
	        "tid": 1002,
	        "url": "https://tamaki.com/iroha",
	        "content": "注意这条推文的hided是true，即被隐藏",
	        "media": "[]",
	        "published": true,
	        "hided": true,
	        "comment": "小淋雨可爱",
	        "newdate": "2020-02-04T13:47:39.000+0800",
	        "updatetime": "2020-02-05T11:33:21.000+0800"
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |url|string|Y|推文完整URL|
   |content|string|Y|推文内容|
   |media|string|Y|推文的media信息|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|一个字典，一共有两个key：`twitter`和`alreadyExist`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`alreadyExist`的键值是一个布尔值，代表在这次插入请求之前对应相同URL的推文是否已经入库过了|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"url": "URL_4","content": "嘤嘤嘤","media":"[]"}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-06T17:10:04.809+08:00",
	    "data": {
	        "twitter": {
	            "tid": 1017,
	            "url": "URL_4",
	            "content": "嘤嘤嘤",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "newdate": "2020-02-06T17:10:04.000+0800",
	            "updatetime": "2020-02-06T17:10:04.000+0800"
	        },
	        "alreadyExist": false
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

   + Request.data[列表元素]
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |url|string|Y|推文完整URL|
   |content|string|Y|推文内容|
   |media|string|Y|推文的media信息|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|一个列表，列表中的每个元素是一个字典。字典一共有两个key：`twitter`和`alreadyExist`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`alreadyExist`的键值是一个布尔值，代表在这次插入请求之前对应相同URL的推文是否已经入库过了|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": [
	    {"url": "URL_1","content": "嘤嘤嘤","media":"[]"},
	    {"url": "URL_2","content": "嘤嘤嘤2","media":"[]"}
	  ]
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-06T15:57:20.669+08:00",
	    "data": [
	        {
	            "twitter": {
	                "tid": 1015,
	                "url": "URL_1",
	                "content": "嘤嘤嘤",
	                "media": "[]",
	                "published": false,
	                "hided": false,
	                "comment": "",
	                "newdate": "2020-02-06T15:57:20.000+0800",
	                "updatetime": "2020-02-06T15:57:20.000+0800"
	            },
	            "alreadyExist": false
	        },
	        {
	            "twitter": {
	                "tid": 1016,
	                "url": "URL_2",
	                "content": "嘤嘤嘤2",
	                "media": "[]",
	                "published": false,
	                "hided": false,
	                "comment": "",
	                "newdate": "2020-02-06T15:57:20.000+0800",
	                "updatetime": "2020-02-06T15:57:20.000+0800"
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
   |data|dict|如果tid在库中无法找到推文，则为**null**。如果找得到，则为一个字典，一共有两个key：`twitter`和`translation`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应最新的翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid": 1016, "img": "[]", "trans":"翻译AA4"}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-06T17:09:20.307+08:00",
	    "data": {
	        "twitter": {
	            "tid": 1016,
	            "url": "URL_2",
	            "content": "嘤嘤嘤2",
	            "media": "[]",
	            "published": false,
	            "hided": false,
	            "comment": "",
	            "newdate": "2020-02-06T15:57:20.000+0800",
	            "updatetime": "2020-02-06T15:57:20.000+0800"
	        },
	        "translation": {
	            "zzid": 30,
	            "version": 0,
	            "translation": "翻译AA4",
	            "img": "[]",
	            "newdate": "2020-02-06T17:09:20.000+0800",
	            "updatetime": "2020-02-06T17:09:20.000+0800"
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |tid|int|Y|整型数，推文的tid|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|如果tid在库中无法找到推文，则为**null**。如果找得到，则为一个字典，一共有两个key：`twitter`和`translation`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构；`translation`的键值是一个字典，对应回滚之后的最新的一条翻译内容入库后的数据结构，如果没有任何翻译存在，则返回**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid": 1002}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-06T17:46:54.911+08:00",
	    "data": {
	        "twitter": {
	            "tid": 1002,
	            "url": "https://tamaki.com/iroha",
	            "content": "第二条含有emoji😁的推文",
	            "media": "[]",
	            "published": true,
	            "hided": true,
	            "comment": "小淋雨可爱",
	            "newdate": "2020-02-04T13:47:39.000+0800",
	            "updatetime": "2020-02-05T17:40:19.000+0800"
	        },
	        "translation": {
	            "zzid": 5,
	            "version": 1,
	            "translation": "BBB2",
	            "img": "b2",
	            "newdate": "2020-02-04T15:45:54.000+0800",
	            "updatetime": "2020-02-04T15:45:54.000+0800"
	        }
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

   + Request.data
	
	|名称|格式|必传|备注|
   |:--|:--|:--|:--|
   |tid|int|Y|整型数，推文的tid|

   + Response

   |名称|格式|备注|
   |:--|:--|:--|
   |code|int|返回码，0成功，其它情况失败，并在msg里附带失败原因的备注|
   |msg|string|备注信息，用来储存异常信息等|
   |data|dict|一个字典，一共有两个key：`twitter`和`translations`，其中`twitter`的键值是一个字典，对应原推文入库后的数据结构，如果tid在库中无法找到推文，则为**null**。`translations`的键值是一个列表，列表中每个元素是一个翻译内容入库后的数据结构，列表按照翻译版本字段`version`降序排序；如果tid在库中无法找到推文，则为**null**|


- 样例
	+ Request [POST]
	
	```json
	{
	  "forwardFrom": "twitkit-app",
	  "timestamp": "2020-01-29T14:40:00.000+08:00",
	  "data": {"tid": 1002}
	}
	```
	
	+ Response
	
	```json
	{
	    "code": 0,
	    "message": "OK",
	    "timestamp": "2020-02-06T17:56:29.592+08:00",
	    "data": {
	        "twitter": {
	            "tid": 1002,
	            "url": "https://tamaki.com/iroha",
	            "content": "第二条含有emoji😁的推文",
	            "media": "[]",
	            "published": true,
	            "hided": true,
	            "comment": "小淋雨可爱",
	            "newdate": "2020-02-04T13:47:39.000+0800",
	            "updatetime": "2020-02-05T17:40:19.000+0800"
	        },
	        "translations": [
	            {
	                "zzid": 5,
	                "version": 1,
	                "translation": "BBB2",
	                "img": "b2",
	                "newdate": "2020-02-04T15:45:54.000+0800",
	                "updatetime": "2020-02-04T15:45:54.000+0800"
	            },
	            {
	                "zzid": 4,
	                "version": 0,
	                "translation": "BBB",
	                "img": "b",
	                "newdate": "2020-02-04T15:45:45.000+0800",
	                "updatetime": "2020-02-04T15:45:45.000+0800"
	            }
	        ]
	    }
	}
	```

	