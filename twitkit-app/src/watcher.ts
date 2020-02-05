import { Context } from 'koishi-core'
import * as http from 'http'
import { parse } from 'url'
import * as utils from './utils'
import { Twitter } from './twitter'
import store from './store'

class request {
    forwardFrom: string
    timestamp: utils.ISO8601
    data: any
}

class rss {
    taskId: utils.uuid
    tid?: number[]
    title?: string
    content: string
    url?: string
    media?: string[]
    author: string
    postDate?: utils.ISO8601
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

let Logger

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
                                    + tw.tid.join(',') + "\n - 发送#-批量隐藏已发推特"
                                    + "\n- 发送" + argv.prefix + "~核对记录的推文是否全部发布"
                                    + "\n- 发送" + argv.prefix + "/将快速搜索起始位置更新为即将到来的下一条推特"
                                    + "\n辛苦了！"
    return msg
}

function sendmsg(ctx: Context, target: any = { discuss: [], private: [], group: [] }, msg: string, tid: number = null): void {
    Logger.debug("msg: " + msg)
    for (const j of target.discuss) {
        ctx.sender.sendDiscussMsgAsync(j, msg)
        Logger.debug("tid: %d, target: G_%d send", tid, j)
    }
    for (const j of target.private) {
        ctx.sender.sendPrivateMsgAsync(j, msg)
        Logger.debug("tid: %d, target: G_%d send", tid, j)
    }
    for (const j of target.group) {
        ctx.sender.sendGroupMsgAsync(j, msg)
        Logger.debug("tid: %d, target: G_%d send", tid, j)
    }
}

export default function (ctx: Context, argv: any = { target: {}, ispro: true, port: 1551, prefix: '#' }) {
    Logger = ctx.logger()
    Logger.debug("watcher server starting...")
    const server = http.createServer((req, res) => {
        let pathname = parse(req.url).pathname;
        Logger.debug(req.method + " " + pathname + " " + req.httpVersion)
        res.writeHead(200, {'Content-Type': 'application/json'})
        let r = /^\/api\/app\/(twitter|other)$/.exec(pathname)
        if (req.method === "POST" && r) {
            req.on("readable", async () => {
                let data: request
                const raw = req.read()
                try {
                    data = JSON.parse(raw)
                    Logger.debug("[" + data.forwardFrom + "] " + data.timestamp + " :")
                    res.end('{"code": 0, "message": "copy"}')
                } catch (e) {
                    Logger.warn("Parse data error: " + e)
                    res.end('{"code": 400, "message": "Data format error"}')
                    return //res.end()
                }
                // return res.end()
            // })
            // req.on("end", async () => {
                switch (r[1]) {
                    case "twitter":
                        for (const i in data.data) if (data.data[i]) {
                            Logger.debug("Event %s, tid: %d", i, data.data[i])
                            const tw:Twitter = await store.get(data.data[i])
                            Logger.debug("[" + tw.user.name + "]" + tw.content)
                            const msg:string = Twitter2msg(tw, argv)
                            sendmsg(ctx, argv.target, msg, data.data[i])
                        } else {
                            Logger.debug("Event %s, no update", i)
                        }
                        Logger.info("Update notice done")
                        break
                    case "other":
                        let rdata: rss
                        try {
                            rdata = <rss> data.data
                            Logger.debug("[" + rdata.postDate + "] " + rdata.content)
                        } catch (e) {
                            Logger.warn("Parse rss data error: " + e)
                        }
                        const msg:string = rss2msg(rdata, argv)
                        Logger.debug("msg: " + msg)
                        sendmsg(ctx, argv.target, msg)
                        Logger.info("Update notice done")
                        break
                    default:
                }
            })
        } else {
            Logger.warn("Unknow request: " + pathname)
            res.write('{"code": 404, "message": "Not found"}')
            res.end()
        }
    })
    try {
        server.listen(argv.port);
        Logger.success("Listening watcher on port %d", argv.port)
    } catch (e) {
        Logger.warn("Listen watcher fail on port " + argv.port + ": " + e)
    }
}