import { Context } from 'koishi-core'
import * as http from 'http'
import url from 'url'
import { uuid, ISO8601 } from './utils'
import { Twitter } from './twitter'
import store from './store'

class request {
    forwardFrom: string
    timestamp: ISO8601
    data: any
}

class rss {
    taskId: uuid
    title?: string
    content: string
    url?: string
    media: string[]
    author?: string
    postDate: ISO8601
}

function Twitter2msg(tw: Twitter): string {
    return
}

function rss2msg(tw: rss): string {
    return
}

export default function (ctx: Context, argv: any = { group: [], ispro: true, port: 1551, prefix: '#' }) {
    const Logger = ctx.logger("info")
    const server = http.createServer({}, (req, res) => {
        let pathname = url.parse(req.url).pathname;
        Logger.debug(req.method + " " + pathname + " " + req.httpVersion)
        res.writeHead(200, {'Content-Type': 'application/json'})
        let r = /^\/api\/app\/(twitter|other)$/.exec(pathname)
        if (req.method === "POST" && r) {
            let data: request
            try {
                data = JSON.parse(req.read())
                Logger.debug("[" + data.forwardFrom + "] " + data.timestamp + " :")
            } catch (e) {
                Logger.error("Parse data error: " + e)
                res.write('{"code": 400, "message": "Data format error"}')
                return res.end()
            }
            switch (r[1]) {
                case "twitter":
                    for (const i in data.data) if (data.data[i]) {
                        Logger.debug("Event %s, tid: %d", i, data.data[i])
                        const tw:Twitter = store.get(data.data[i])
                        Logger.debug("[" + tw.user.name + "]" + tw.content)
                        const msg:string = Twitter2msg(tw)
                        for (const j of argv.group) {
                            ctx.sender.sendGroupMsgAsync(j, msg)
                            Logger.debug("tid: %d, target: G_%d send", data.data[i], j)
                        }
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
                        Logger.error("Parse rss data error: " + e)
                    }
                    const msg:string = rss2msg(rdata)
                    for (const j of argv.group) {
                        ctx.sender.sendGroupMsgAsync(j, msg)
                        Logger.debug("rss, target: G_%d send", j)
                    }
                    Logger.info("Update notice done")
                    break
                default:
            }
        } else {
            Logger.warn("Unknow request: " + pathname)
            res.write('{"code": 404, "message": "Not found"}')
        }
        res.end()
    })
    try {
        server.listen(argv.port);
        Logger.success("Listening watcher on port %d", argv.port)
    } catch (e) {
        Logger.error("Listen watcher fail on port " + argv.port + ": " + e)
    }
}