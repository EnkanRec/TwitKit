import { Context, Logger } from 'koishi-core'
import { Twitter } from './twitter'
import { request } from './utils'
import axios from 'axios'

let host: string
let logger: Logger

class response { // oven return
    code: number
    message: string
    error?: any
    processTime?: number
    resultUrl?: string
    tid?: number
}

/**
 * @description 封装api请求，但仅用于bake/bakeurl
 * @param url api地址
 * @param data 数据
 * @returns response.resultUrl
 */
async function rest(url: string, data: any): Promise<string> {
    logger.debug("POST " + url)
    logger.debug(data)
    try {
        const res = await axios.post<response>(host + url, new request(data))
        if (res.data.code === 0) {
            logger.debug(res.data)
            logger.debug("Return %d: %s", res.data.code, res.data.message || "")
            logger.debug("Finish in %d s", res.data.processTime)
        return res.data.resultUrl
        } else {
            logger.warn("Error %d: %s", res.data.code, res.data.message)
            return null
        }
    } catch (e) {
        if (e.response) {
            logger.error("Internet error: %d", e.response.status)
            logger.debug(e.response.data)
        } else logger.error(e)
    }
    return null
}

/**
 * @description 请求一个推文的烤图结果
 * @param tw 推文内容
 * @param tw.id 推文id
 * @param tw.trans 推文翻译
 * @returns 烤推结果图片url
 */
function get(tw: Twitter): Promise<string> {
    logger.debug("Tid: %d", tw.id)
    return rest("/api/oven/bake", {
        tid: tw.id,
        transText: tw.trans
    })
}

/**
 * @description 请求特定url的烤图结果
 * @param url 特定推特url
 * @param trans 翻译
 * @returns 烤推结果图片url
 */
function getByUrl(url: string, trans: string): Promise<string> {
    logger.debug("url: " + url)
    return rest("/api/oven/bake", {
        url,
        transText: trans
    })
}

/**
 * @description 检查图片是否是烤推发布
 * @param url 需要检查的图片url
 * @returns 检测出的推文id
 * @deprecated 暂未使用
 */
async function check(url: string): Promise<number> {
    let res = await axios.post<response>(host + "/api/oven/check", new request({
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
    host = Host
    logger.info("translator client ready")
}

export default {
    init,
    get,
    getByUrl,
    check
}