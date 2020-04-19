# twitkit-app

基于koishi框架的APP部分，业务逻辑依赖于其他组件

## 输入接口

### 指令

除了使用koishi的commend创建的普通命令，还用middleware实现了仿原bot的短指令

涉及的三个变量：

* 最新一条任务id(last)：数据库持久化变量，既数据表自增id，无缓存
* 最近修改过的翻译(lastTrans)：非持久化变量，用户使用`translate`（带翻译）同时更新，缓存在内存
* 队列头(todo)：数据库持久化变量，由用户使用`clear`命令更新，启动时从数据库获取缓存在内存

#### 短指令/快捷命令

<detail>
<summary>基本格式 `"指令前缀[任务id][指令符] [附加文字]"`</summary>

除了附加文字，各部分之间没有空格

指令前缀单独配置，默认为 `#`

| 指令符 | 有id | 例子1 | 等价长指令1 | 无id | 例子2 | 等价长指令2 |
|:-----:|:----:|:-----:|:----------:|:----:|:----:|:----------:|
| <无> | 查看该任务原文或烤图（若存在） | #1000 | translate 1000 | 列出队列所有任务状态 | # | list |
| ! | 不更新翻译刷新烤图 | #1000! | fresh 1000 | 刷新最近修改过的翻译的烤图 | #! | fresh |
| * | 显示推文原文 | #1000! | raw 1000 | 显示最近修改过的翻译服推文原文 | #* | raw |
| ~ | 列出该任务之后的所有任务状态 | #1000~ | list 1000 | 列出队列所有任务状态 | #~ | list |
| ~~ | 列出该任务之后的所有烤推结果（及媒体） | #1000~~ | list-detail 1000 | 列出队列所有烤推结果（及媒体） | #~~ | list-detail |
| / | 设置队列头为该id | #1000/ | clear 1000 | 清空队列（设置队列头为最新的任务id） | #/ | clear |
| - | 隐藏/显示该任务 | #1000- | hide 1000 | 批量隐藏队列里已发布的任务（相当于移出队列） | #- | hide |
| + | 给该任务添加备注 | #1000+[备注] | comment 1000 [备注] | 给最后一条任务添加备注 | #+[备注] | comment [备注] |
| ? | | | | 显示这条帮助 | #? | |

其他无指令符指令

| 参数1 | 参数2 | 功能 | 例子 | 等价长指令 |
|:-----:|:----:|:----:|:----:|:---------:|
| id | 翻译 | 更新该任务翻译 | #1000 [翻译] | translate 1000 [翻译] |
| url | <无> | 将指定链接的推文入库 | #<https://twitter.com/any/status/12345> | fetch [url] |
| url | 翻译 | 直接对链接烤图 | #<https://twitter.com/any/status/12345> [翻译] | fetch [url] [翻译] |
</detail>

#### 长指令

<detail>
<summary>指令前缀参考koishi指令帮助，默认为空，可以在群聊中直接使用如 `undo`</summary>

指令前缀

| 指令 | 使用 | 介绍 |
|:----:|:---:|:----:|
| translate | translate &lt;tid&gt; [trans] | 获取/更新这个id的翻译内容 |
| fetch | fetch &lt;url&gt; [trans] | 将指定链接的推文入库或直接烤图 |
| fresh | fresh [tid] | 刷新这个id的翻译烤图，id为空时使用最近修改过的翻译 |
| raw | raw [tid] | 显示这个id的推文原文，id为空时使用最近修改过的翻译 |
| list | list [tid] | 查看队列某个id后的任务，id为空时使用预设的队列头 |
| list-detail | list-detail [tid] | 批量获取队列某个id后的烤推结果，id为空时使用预设的队列头 |
| clear | clear [tid] | 设置队列头，id为空时，设置成最新的id（清空队列） |
| hide | hide [tid] | 隐藏或显示某个推，id为空时，隐藏所有已烤的推 |
| comment | comment [tid] &lt;text&gt; | 为某个推添加注释，id为空时，加到最近的推 |
| undo | undo [tid] | 撤销某个推的翻译修改，id为空时，撤销最近修改过的翻译 |
| delete| delete &lt;tid&gt; | 删除一个任务，返回是否删除成功，建议使用hide隐藏 |
| how | how | 显示内置帮助 |
</detail>

### 监听器

<detail>
<summary>REST基本请求格式：</summary>

HTTP method: `POST`

content-type: `application/json`

body：（均非空）

| 字段 | 类型 | 注释 |
|:----:|:---:|:----:|
| forwardFrom | string | 请求来源名 |
| timestamp | string | ISO8601格式的日期字符串 |
| taskId | string | 一个uuid，用于调试跟踪执行过程 |
| data | any | 接口需要的数据，见下 |
</detail>

#### 推特更新

<detail>
<summary>路由：`/api/app/twitter`</summary>

data: number[] 所有新到达的任务id

通知app有哪些新任务
</detail>

#### 其他更新

<detail>
<summary>路由：`/api/app/other`</summary>

data: dict （无注明既可空说）其他更新的详细信息

| 字段 | 类型 | 注释 |
|:----:|:---:|:----:|
| tid | number[] | 识别到的烤推发布 |
| title | string | 标题 |
| content |string | 非空，正文 |
| url | string | 原始链接 |
| media | string[] | 媒体图片链接 |
| author | string | 非空，发布人 |
| postDate | string | 发布日期，ISO8601格式 |

将非任务更新，推送给app以便在群里通知
</detail>

## 输出接口

### 数据库

见 `store.ts`

### 烤推机

见 `translator.ts`

## 配置

```js
{
    prefix: '#',      // 快捷指令前缀
    ispro: false,     // 是否发图
    cmd: {
        host: {       // 其他组件的REST监听HOST
            store: "http://localhost:8220",
            translator: "http://localhost:8221",
            maid: "http://localhost:8222",
        },
        group: [],      // 监听命令的群组，留空监听所有人
        private: true,  // 是否允许私聊上班
        friend: true,   // 是否允许好友上班
        cut: 8          // 消息预览截断长度
    },
    watcher: {
        port: 8223,     // 监视器推送端口
        target: {       // 监视器更新推送目标
            discuss: [],
            private: [],
            group: []
        }
    }
}
```

## 启动

1. 安装[koishi](https://koishi.js.org/)
2. 安装依赖 `npm install`
3. 编写配置，参考 `.koishi.config.js`
4. 编译 `npm run build`
5. 运行 `koishi run` 或 `npm run dev`
