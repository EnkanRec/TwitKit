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
    rootTid?: number[]
}

async function rest(url: string, data: any): Promise<response> {
    logger.debug("Data: " + data)
    let res = await axios.post<response>(host + url, new request(data))
    if (res.status !== 200) {
        logger.error("Internet error: %d", res.status)
        return null
    }
    logger.debug("Return %d: %s", res.data.code, res.data.message)
    if (res.data.code === 0) {
        return res.data
    } else {
        logger.warn("Error: %s", res.data.error)
        return null
    }
}

async function addTask(url: string): Promise<number[]> {
    const res = await rest("/api/waitress/addtask", { url })
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