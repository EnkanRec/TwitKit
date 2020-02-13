# Twitkit-Oven

## 环境准备

环境要求：

* Python 3.6及以上
* wkhtmltopdf

以下步骤以在Ubuntu 18.04的环境下为例。

1. Oven使用wkhtmltopdf的`wkhtmltoimage`从网页生成图片，可用以下命令安装：
    ```
    apt install wkhtmltopdf
    ```

2. 安装Python环境，创建虚拟环境（也可以直接装全局）：

    暂略

3. 安装Python依赖：

    ```
    pip install -r requirements.txt
    ```

4. 安装Gunicorn和gevent（生产环境需要）：
    ```
    pip install gunicron gevent
    ```

5. 准备配置文件：

    * 将`config_example.py`复制一份，命名为`config.py`，根据需要修改配置（下面介绍）；
    * 如果是生产环境，也将`gunicorn_config_example.py`复制一份，命名为`gunicorn_config.py`，根据需要修改配置。（配置文件设置项可参考[文档](http://docs.gunicorn.org/en/stable/settings.html)）

6. 安装字体：
   
    从<https://github.com/adobe-fonts/source-han-sans/releases>下载[SourceHanSans.ttc](https://github.com/adobe-fonts/source-han-sans/releases/download/2.001R/SourceHanSans.ttc)，放入`/usr/local/share/fonts/`。此为全语言全字重的字体集，比较方便。或者用其他字体也可以。可执行`fc-list`确认字体已装好。

## 配置文件说明

Oven从`config.py`中的变量读入配置。变量名和说明如下。

### 渲染设置

* `VIEWPORT_WIDTH`：视口宽度。96PPI时，一个像素等于一个真实像素。即例如设为480的话，96PPI时出图为480px。
* `DEFAULT_PPI`：默认PPI（Pixels Per Inch）。如果Oven的API调用的时候没有指定PPI，会用这里的值。
* `ZH_FONT`：中文字体。假如按上面的步骤安装了Source Han Sans的TTC，这里填写`Source Han Sans SC`。
* `JA_FONT`：日文字体。假如按上面的步骤安装了Source Han Sans的TTC，这里填写`Source Han Sans`。

### 二维码定位设置
此二维码用于识别tid。以下数值对应的均为相对于浏览器渲染时的像素值，和上面视口宽度类似。

* `TID_CODE_WIDTH`：tid二维码宽度
* `TID_CODE_HEIGHT`：tid二维码宽度

* `TID_CODE_POS_X`：tid二维码水平定位（若填写负数，表示从右起）
* `TID_CODE_POS_Y`：tid二维码垂直定位（若填写负数，表示从下起）

### URL设置
* `EXT_STATIC_BASE_URL`：指向`/static`的对外URL前缀，返回输出图片URL时用，例如`https://example.local/images`。
* `INT_BASE_URL`：内部URL前缀，在`wkhtmltoimage`访问内部生成的推文页面时用。如果Gunicorn配置里改了端口号，这里要相应修改。

### 日志设置
* `LOG_LEVEL`：日志等级。例如`INFO`、`DEBUG`。
* `APP_LOG_FILE`：日志文件路径。设None、False或者空字串不输出日志文件。


## 运行服务端

### 生产环境

生产环境下，用`gunicorn`运行`app`。

```
gunicorn -c gunicorn_config.py app:app
```

### 开发环境

用Python执行`app.py`即可。

`tests.py`中有少量测试，目前只有针对关于tid二维码的。

## API说明
💡 **访问web根目录可浏览API。**

### 提交烤图任务

接口地址：`/api/oven/bake`

请求和返回报文均为标准JSON格式

提交方法：POST

#### 请求报文

请求报文样例：

```json
{
  "forwardFrom": "twitkit-stargazer",
  "timestamp": "2020-01-29T14:23:23.233+08:00",
  "data": {
    "taskId": "123e4567-e89b-12d3-a456-426655440000",
    "tid": 1001,
    "origText": "マギアレコードの新魔法少女「南津　涼子」のキャラデザを担当させていただきました！どうぞよろしくお願いします✨",
    "transText": "我有幸担任了魔法纪录新魔法少女“南津 凉子”的人物设计！请多多指教✨",
    "media": [
      "https://pbs.twimg.com/media/EPcgk0JUUAE3NLt?format=jpg&name=orig"
    ],
    "username": "magireco",
    "postDate": "2020-01-29T14:23:23.233+08:00",
  }
}
```



| 名称          | 格式   | 必传 | 备注                                                         |
| ------------- | ------ | ---- | ------------------------------------------------------------ |
| `forwardFrom` | string | Y    | 一个字符串，标定是哪个服务调用了此接口，如`twitkit-stargazer` |
| `timestamp`   | string | Y    | 请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00` |
| `data`        | dict   | Y    | 请求数据（见下表）                                           |

`data`参数内：

| 名称                | 格式   | 必传 | 备注                                                         |
| ------------------- | ------ | ---- | ------------------------------------------------------------ |
| `taskId`            | string | Y    | 一个UUID，是一个任务的上下文唯一标识符                       |
| `tid`               | int    | Y    | 推文ID                                                       |
| `origText`          | string | N    | 推文原文，Plain Text格式，不传则只输出译文                   |
| `transText`         | string | N    | 推文译文，Plain Text格式，不传则只输出原文<br />（`origText`和`transText`必须有一个出现） |
| `media`             | list   | N    | 媒体列表，列表里每个字符串是一个媒体URL                      |
| `username`          | string | Y    | 推特用户名，转推时是原推作者用户名                           |
| `retweeterUsername` | string | N    | 转推时需要传此参数，为转发者的用户名                         |
| `postDate`          | string | Y    | 发推日期时间，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00` |
| `ppi`               | int    | N    | 生成图像PPI，默认144                                         |

#### 返回报文

若请求成功，返回报文内容例：

```json
{
  "code": 0,
  "message": "OK",
  "processTime": 666,
  "resultUrl": "https://example.local/static/56d9345e6a95d806e52a146260f801bd.png"
}
```

若请求参数不符合要求，返回报文内容例：

```json
{
  "code": 400,
  "errors": {
    "data.tid": "'tid' is a required property"
  },
  "message": "Input payload validation failed"
}
```

参数说明：


| 名称          | 格式   | 必传 | 备注                                           |
| ------------- | ------ | ---- | ---------------------------------------------- |
| `code`        | int    | Y    | 返回码，0为成功，其他情况见下表                |
| `message`     | string | Y    | 备注消息                                       |
| `processTime` | int    | N    | 处理用时（毫秒，仅出图成功时）                 |
| `resultUrl`   | string | N    | 输出图片URL（仅出图成功时）                    |
| `errors`      | dict   | N    | 请求参数不符合要求时使用，包含失败的参数键值对 |

目前可能出现的返回码：

| 返回码 | 意义             |
| ------ | ---------------- |
| 0      | 输出图片成功     |
| 400    | 输入格式验证失败 |
| 500    | 输出图片失败     |

### 查询图片对应tid

接口地址：`/api/oven/check`

请求和返回报文均为标准JSON格式

提交方法：POST

#### 请求报文

请求报文样例：

```json
{
  "forwardFrom": "twitkit-stargazer",
  "timestamp": "2020-01-29T14:23:23.233+08:00",
  "data": {
    "taskId": "123e4567-e89b-12d3-a456-426655440000",
    "imageUrl": "https://example.com/some-image.png"
  }
}
```



| 名称          | 格式   | 必传 | 备注                                                         |
| ------------- | ------ | ---- | ------------------------------------------------------------ |
| `forwardFrom` | string | Y    | 一个字符串，标定是哪个服务调用了此接口，如`twitkit-stargazer` |
| `timestamp`   | string | Y    | 请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00` |
| `data`        | dict   | Y    | 请求数据（见下表）                                           |

`data`参数内：

| 名称       | 格式   | 必传 | 备注                                   |
| ---------- | ------ | ---- | -------------------------------------- |
| `taskId`   | string | Y    | 一个UUID，是一个任务的上下文唯一标识符 |
| `imageUrl` | string | Y    | 要查询tid的图片的URL                   |

#### 返回报文

若请求成功，返回报文内容例：

```json
{
  "code": 0,
  "message": "OK",
  "tid": 2233
}
```

若请求参数不符合要求，返回报文内容例：

```json
{
  "code": 400,
  "errors": {
    "data.url": "'url' is a required property"
  },
  "message": "Input payload validation failed"
}
```

参数说明：


| 名称          | 格式   | 必传 | 备注                                           |
| ------------- | ------ | ---- | ---------------------------------------------- |
| `code`        | int    | Y    | 返回码，0为成功，其他情况见下表                |
| `message`     | string | Y    | 备注消息                                       |
| `processTime` | int    | N    | 处理用时（毫秒，仅处理成功时）            |
| `tid`   | string | N    | 结果tid（仅处理成功时；若图片中不含有效的tid信息则返回-1） |
| `errors`      | dict   | N    | 请求参数不符合要求时使用，包含失败的参数键值对 |

目前可能出现的返回码：

| 返回码 | 意义                 |
| ------ | -------------------- |
| 0      | 处理成功             |
| 400    | 输入格式验证失败     |
| 500    | 后端出错导致处理失败 |