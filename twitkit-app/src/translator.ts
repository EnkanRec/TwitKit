import { Context, Logger } from 'koishi-core'
import { Twitter } from './twitter'
import * as utils from './utils'
import axios from 'axios'

let host: string
let logger: Logger

class response { // oven return
    code: number
    message: string
    error?: any
    processTime: number
    resultUrl?: string
    tid?: number
}

async function get(tw: Twitter): Promise<string> {
    logger.debug("Tid: %d", tw.id)
    let res = await axios.post<response>(host + "/api/oven/bake", new utils.request({
        // taskId: utils.genUuid(),
        tid: tw.id,
        origText: tw.content,
        transText: tw.trans,
        media: tw.media,
        username: tw.user.id,
        origUid: tw.user.name,
        postDate: tw.postDate
    }))
    if (res.status !== 200) {
        logger.error("Internet error: %d", res.status)
        return null
    }
    logger.debug("Return %d: %s", res.data.code, res.data.message)
    if (res.data.code === 0) {
        logger.debug("Finish in %d s", res.data.processTime)
        return res.data.resultUrl
    } else {
        logger.warn("Error: %s", res.data.error)
        return null
    }
}

async function getByUrl(url: string, trans: string): Promise<string> {
    return null
}

async function check(url: string): Promise<number> {
    let res = await axios.post<response>(host + "/api/oven/check", new utils.request({
        // uuid: utils.genUuid(),
        imageUrl: url
    }))
    if (res.status !== 200) {
        logger.error("Internet error: %d", res.status)
        return null
    }
    logger.debug("Return %d: %s", res.data.code, res.data.message)
    if (res.data.code === 0) {
        logger.debug("Found tid %d", res.data.tid)
        return res.data.tid
    } else {
        logger.warn("Error: %s", res.data.error)
        return null
    }
}

function init(ctx: Context, Host: string): void {
    logger = ctx.logger("app:translator")
    host = Host || "http://localhost"
    logger.debug("translator client ready")
}

export default {
    init,
    get,
    getByUrl,
    check
}