import { Context, Logger, MessageType } from 'koishi-core'
import * as http from 'http'
import { parse } from 'url'
import * as utils from './utils'
import { Twitter, Twitter2msg } from './twitter'
import store from './store'
import translator from './translator'

let logger: Logger

class rss {
    taskId: utils.uuid
    tid?: number[]
    title?: string
    content: string
    url?: string
    media?: string[]
    author: string
    postDate?: utils.ISO8601

    static parse(Data: rss): rss {
        if (typeof Data.taskId === "undefined"
            || typeof Data.content === "undefined"
            || typeof Data.author === "undefined"
            || typeof Data.postDate !== "undefined"
            && !utils.verifyDatetime(Data.postDate)
        ) throw "param lost"
        return Data
    }
}

async function rss2msg(tw: rss, argv): Promise<string> {
    let msg: string = "【" + tw.author + "】"
    if (tw.title) msg += tw.title
    msg += "\n----------------\n内容: " + tw.content
    if (tw.media) {
        msg += "\n媒体: "
        if (!tw.tid) tw.tid = []
        for (const img of tw.media) {
            msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
            // 检查是否为烤推发布；未启用
            // const tid: number = await translator.check(img)
            // if (tid) tw.tid.push(tid)
        }
    }
    if (tw.url) msg += "\n原链接: " + tw.url
    if (tw.tid && tw.tid.length) msg += "\n识别到本条发布包含" + tw.tid.length + "条烤推结果: "
        + tw.tid.join(', ') + "\n - 发送#-批量隐藏已发推特"
        + "\n- 发送" + argv.prefix + "~核对记录的推文是否全部发布"
        + "\n- 发送" + argv.prefix + "/将快速搜索起始位置更新为即将到来的下一条推特"
        + "\n辛苦了！"
    return msg
}

function sendmsg(ctx: Context, target: { discuss: number[], private: number[], group: number[] }, msg: string): void {
    logger.debug("msg: " + msg)
    for (const i in target) for (const j of target[i]) {
        ctx.sender.sendMsgAsync(<MessageType>i, j, msg)
        logger.debug("send message to: %s_%d", i, j)
    }
}

export default function (ctx: Context, argv: utils.config) {
    logger = ctx.logger("app:watcher")
    logger.debug("watcher server starting...")
    const server = http.createServer((req, res) => {
        let pathname = parse(req.url).pathname;
        logger.debug(req.method + " " + pathname + " HTTP " + req.httpVersion)
        res.writeHead(200, { 'Content-Type': 'application/json' })
        let r = /^\/api\/app\/(twitter|other)$/.exec(pathname)
        if (req.method === "POST" && r) {
            let raw: string = ""
            req.on("data", (chunk) => { raw += chunk })
            req.on("end", async () => {
                let data: utils.request
                logger.debug(raw)
                try {
                    data = utils.request.parse(JSON.parse(raw))
                    logger.info("[" + data.forwardFrom + "] " + data.timestamp + " :")
                    res.end(new utils.response("copy").toString())
                } catch (e) {
                    logger.warn("Parse data error: " + e)
                    res.end(new utils.response("Data format error", 400).toString())
                    return
                }
                logger.debug(data.data)
                switch (r[1]) {
                    case "twitter":
                        for (const i in data.data) if (data.data[i]) {
                            logger.debug("Event %s, tid: %d", i, data.data[i])
                            const tw: Twitter = await store.getTask(data.data[i])
                            logger.debug("[" + tw.user.name + "]" + tw.content)
                            const msg: string = Twitter2msg(tw, argv)
                            sendmsg(ctx, argv.target, msg)
                        } else {
                            logger.debug("Event %s, no update", i)
                        }
                        logger.info("Update notice done")
                        break
                    case "other":
                        let rdata: rss
                        try {
                            rdata = rss.parse(data.data)
                            logger.debug("[" + rdata.postDate + "] " + rdata.content)
                        } catch (e) {
                            logger.warn("Parse rss data error: " + e)
                            return
                        }
                        const msg: string = await rss2msg(rdata, argv)
                        sendmsg(ctx, argv.target, msg)
                        logger.info("Update notice done")
                        break
                    default:
                }
            })
        } else {
            logger.warn("Unknow request: " + pathname)
            res.end(new utils.response("Not Found", 404).toString())
        }
    })
    try {
        server.listen(argv.port);
        logger.success("Listening watcher on port %d", argv.port)
    } catch (e) {
        logger.warn("Listen watcher fail on port " + argv.port + ": " + e)
    }
}