# Twitkit-Oven

## 环境准备

环境要求：

* Python 3.6及以上
* wkhtmltopdf
* Xvfb（或者图形环境）

以下步骤以在Ubuntu 18.04的环境下为例。

1. Oven使用wkhtmltopdf的`wkhtmltoimage`从网页生成图片，在非GUI环境下需要用Xvfb在无头环境下运行，可用以下命令安装：
    ```
    apt install wkhtmltopdf xvfb
    ```

2. 安装Python依赖：

    ```
    pip3 install -r requirements.txt
    ```

3. 将`config_example.py`复制一份，命名为`config.py`，根据需要修改配置（下面介绍）；

4. 安装字体：

    ```
    apt install fonts-noto-cjk
    ```

    * 也可以使用其他字体，需要修改配置文件里相应的配置项；

    * 如果要支持一些不常见的符号，可从Windows复制一份Segoe UI Symbol字体至Ubuntu（ 从Windows复制`%WINDIR%\Fonts\seguisym.ttf`放入`/usr/local/share/fonts/`，可执行`fc-list`确认字体已装好）。

5. 构建Baker（渲染推文用的网页）：
   
    先安装Yarn（可能需要sudo）
    ```
    npm install -g yarn
    ```
    
    然后安装依赖，构建
    ```
    cd baker
    yarn install
    yarn build
    ```
    （以后考虑在release里包含构建好的`dist`文件）
    
## 配置文件说明

Oven从`config.py`中的变量读入配置。变量名和说明如下。

### 渲染设置

* `VIEWPORT_WIDTH`：视口宽度。96PPI时，一个像素等于一个真实像素。即例如设为480的话，96PPI时出图为480px

* `DEFAULT_PPI`：默认PPI（Pixels Per Inch）。如果Oven的API调用的时候没有指定PPI，会用这里的值

* `ZH_FONT`：中文字体。假如按上面的步骤安装了Noto Sans，保持默认即可

* `JA_FONT`：日文字体。假如按上面的步骤安装了Noto Sans，保持默认即可

* `JAVASCRIPT_DELAY`：JavaScript延时（毫秒，如果网页无法在此时间限制内载完，需增大此值）

### 监听设置

* `API_SERVER_HOST`：监听地址

* `API_SERVER_PORT`：监听端口

### 二维码设置

此二维码用于识别tid。以下定位数值对应的均为相对于浏览器渲染时的像素值，和上面视口宽度类似。

* `TID_CODE_WIDTH`：tid二维码宽度

* `TID_CODE_HEIGHT`：tid二维码宽度

* `TID_CODE_POS_X`：tid二维码水平定位（若填写负数，表示从右起）

* `TID_CODE_POS_Y`：tid二维码垂直定位（若填写负数，表示从下起）

* `TID_CODE_KEY`：tid二维码的key，由不同实例产生的图需要发在同一个Bilibili账号上时，可修改此值避免冲突，key可以是0到255的整型。

### URL设置

* `EXT_STATIC_BASE_URL`：指向`/static`的对外URL前缀，返回输出图片URL时用，例如`https://example.local/images`

* `INT_BASE_URL`：内部URL前缀，在`wkhtmltoimage`访问内部生成的推文页面时用。如果Gunicorn配置里改了端口号，这里要相应修改

* `FRIDGE_API_BASE`：指向Fridge API的Base URL，用于从数据库烤推，例如`http://127.0.0.1:10103/api`

* `MAID_API_BASE`：指向Maid API的Base URL，用于URL烤推，例如`http://127.0.0.1:5001/api`

### 日志设置

* `LOG_DEBUG`：是否打印调试日志

* `LOG_FILE`：日志文件路径。设`None`不输出日志文件


## 运行服务端

在Xvfb下，用Python3执行`start_oven.py`即可，例如：

```
xvfb-run python3 start_oven.py
```

如果有图形环境，可以不用`xvfb-run`

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
    "transText": "译文"
  }
}
```



| 名称          | 格式   | 必传 | 备注                                                         |
| ------------- | ------ | ---- | ------------------------------------------------------------ |
| `forwardFrom` | string | Y    | 一个字符串，标定是哪个服务调用了此接口，如`twitkit-stargazer` |
| `timestamp`   | string | Y    | 请求发出时间戳，ISO8601格式，标准样例`2020-01-29T14:23:23.233+08:00` |
| `data`        | dict   | Y    | 请求数据（见下表）                                           |

`data`参数内：

| 名称        | 格式   | 必传 | 备注                                               |
| ----------- | ------ | ---- | -------------------------------------------------- |
| `taskId`    | string | Y    | 一个UUID，是一个任务的上下文唯一标识符             |
| `tid`       | int    | N    | 任务ID（从任务列表烤推时，传此参数）               |
| `url`       | string | N    | 推特URL（URL烤推时，传此参数）                     |
| `transText` | string | N    | 推文译文，Plain Text格式，不传则使用数据库中的译文 |
| `ppi`       | int    | N    | 生成图像PPI，默认144                               |

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