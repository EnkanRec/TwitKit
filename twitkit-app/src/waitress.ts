import { Context, Logger } from 'koishi-core'
import { request } from './utils'
import axios from 'axios'

let host: string
let logger: Logger

class response { // oven return
    code: number
    message: string
    error?: any
    addedTid?: number[]
    rootTid?: number
}

async function rest(url: string, data: any): Promise<response> {
    logger.debug("POST " + url)
    logger.debug(data)
    try {
        const res = await axios.post<response>(host + url, new request(data))
        if (res.data.code === 0) {
            logger.debug("Return %d: %s", res.data.code, res.data.message || "")
            logger.debug(res.data)
            return res.data
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

async function addTask(url: string): Promise<number[]> {
    const res = await rest("/api/maid/addtask", { url })
    if (!res || !res.rootTid || !res.addedTid) return null
    return res.addedTid
}

function init(ctx: Context, Host: string) {
    logger = ctx.logger("app:waitress")       // 初始化logger
    host = Host                               // 初始化waitress的Host
    logger.info("waitress ready")
}

export default {
    addTask,
    init
}