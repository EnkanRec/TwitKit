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
        taskId: utils.genUuid(),
        tid: tw.id,
        origText: tw.content,
        transText: tw.trans,
        media: tw.media,
        username: tw.user.id || tw.user.name,
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
    return "base64://iVBORw0KGgoAAAANSUhEUgAAAFsAAAAMCAIAAACC31aBAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAIHSURBVEhLrVXbkQIxDKMuCko9W802QzFgOX7HCdzM6eeytiwpXuAe74p7PMYtZ8frej6+opnr1Rj3eF4vOU/8Rj6wHJS3qk/gImHcxPiCGMFGskV62goz74dkh/j/thEkCWDiJriWTc5mjS2fEWpoyQ2xtk4WoF4THhPfIYOWqp0y0jz1GwlHvaugPE7EdyDnoCDwb43SPUPlBtxkuDo26FWoaggyDdnTGE9LWKQP2wqitgGttUEyyZIffv0dSbjHuOO+Dzio+VvqoV2ed9YYKGEdSbh8KKIvndusHCDb0wxtJNdWFDWyhhfLLbkIJ7kktdlpvpreLJDZYBk9b2SeS7SafGJupO8xSu6gTnUCu9R8JR6A5eWa5KOvH/9dwD7qlzfS5M2W6Sln5hIzUIxidKZ+2Aj9zZBqUrGHUle6QgwVYPjz5DcvGogmMVxwzlYT/RjDHsoNUZQed+Z83oirhKoYYSj1QwCA+qniS8BkIQOukJz/tBE/65YR0q0ZRoqZiYMi9eaMlOtGMqTKKm4tWDbSYfULSAquv98I/YPDaq8QhTXERafwGLP5foSoQNF9Cfyw/Ywodjf/shE1ny+MsLI3CvLyBKoDCD3kzNsD2C1fg/g26WyxiZeeattfVpZmNE1Ce5/DkLeoJ/fMJGOYMNNWG5lmJAk0lB78jCSZZZ7PkYbS6/0BwhLl3Ql1BCMAAAAASUVORK5CYII="
}

async function check(url: string): Promise<number> {
    let res = await axios.post<response>(host + "/api/oven/check", new utils.request({
        uuid: utils.genUuid(),
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