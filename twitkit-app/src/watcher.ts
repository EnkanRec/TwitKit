import { Context, Logger } from 'koishi-core'
import * as http from 'http'
import { parse } from 'url'
import * as utils from './utils'
import { Twitter } from './twitter'
import store from './store'

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
            && !utils.varifyDatetime(Data.postDate)
        ) throw "param lost"
        return Data
    }
}

function Twitter2msg(tw: Twitter, argv = { ispro: true, prefix: '#' }): string {
    let msg: string = "【" + tw.user.name + "】" + tw.type
        + "\n----------------\n"
        + "内容：" + tw.content
    if (tw.media) {
        msg += "\n媒体："
        for (const img of tw.media) msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
    }
    msg += "\n原链接：" + tw.url + "\n快速嵌字发送：" + argv.prefix + tw.id + " 译文"
    return msg
}

function rss2msg(tw: rss, argv = { ispro: true, prefix: '#' }): string {
    let msg: string = "【" + tw.author + "】"
    if (tw.title) msg += tw.title
    msg += "\n----------------\n内容：" + tw.content
    if (tw.media) {
        msg += "\n媒体："
        for (const img of tw.media) msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
    }
    if (tw.url) msg += "\n原链接：" + tw.url
    if (tw.tid && tw.tid.length) msg += "\n识别到本条发布包含" + tw.tid.length + "条烤推结果："
        + tw.tid.join(', ') + "\n - 发送#-批量隐藏已发推特"
        + "\n- 发送" + argv.prefix + "~核对记录的推文是否全部发布"
        + "\n- 发送" + argv.prefix + "/将快速搜索起始位置更新为即将到来的下一条推特"
        + "\n辛苦了！"
    return msg
}

function sendmsg(ctx: Context, target: any = { discuss: [], private: [], group: [] }, msg: string, tid: number = null): void {
    logger.debug("msg: " + msg)
    for (const j of target.discuss) {
        ctx.sender.sendDiscussMsgAsync(j, msg)
        logger.debug("tid: %d, target: D_%d send", tid, j)
    }
    for (const j of target.private) {
        ctx.sender.sendPrivateMsgAsync(j, msg)
        logger.debug("tid: %d, target: P_%d send", tid, j)
    }
    for (const j of target.group) {
        ctx.sender.sendGroupMsgAsync(j, msg)
        logger.debug("tid: %d, target: G_%d send", tid, j)
    }
}

export default function (ctx: Context, argv: any = { target: {}, ispro: true, port: 1551, prefix: '#' }) {
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
                            const tw: Twitter = await store.get(data.data[i])
                            logger.debug("[" + tw.user.name + "]" + tw.content)
                            const msg: string = Twitter2msg(tw, argv)
                            sendmsg(ctx, argv.target, msg, data.data[i])
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
                        const msg: string = rss2msg(rdata, argv)
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