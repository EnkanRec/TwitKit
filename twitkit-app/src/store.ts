import { Context, Logger } from 'koishi-core'
import { Twitter, convert, dbtw } from './twitter'
import { request, response } from './utils'
import axios from 'axios'

let host: string
let logger: Logger
let todo: number = 0
let lastTrans: number

/**
 * @description 统一包装请求，处理通用错误
 * @param url api路由
 * @param data api数据
 * @returns error ? null : data || true
 */
async function rest(url: string, data?: any): Promise<any> {
    logger.debug("POST " + url)
    logger.debug(data)
    try {
        const res = await axios.post<response>(host + url, new request(data))
        if (res.data.code === 0) {
            logger.debug("Return %d: %s", res.data.code, res.data.msg || "")
            logger.debug(res.data.data)
            if (typeof res.data.data === "undefined") return true
            return res.data.data
        } else {
            logger.warn("Error %d: %s", res.data.code, res.data.msg)
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
 * @description 更新设置
 * @param key 目标设置项
 * @param value 值
 */
function setKV(key: string, value: string): Promise<void> {
    return rest("/api/db/kv/set", { [key] : value })
}

/**
 * @description 获取多个设置
 * @param keys 所有目标设置
 * @returns value[]
 */
function getKVs(keys: string[]): Promise<any> {
    return rest("/api/db/kv/get", keys)
}

/**
 * @description 获取单个设置
 * @param key 目标设置项
 * @returns error ? null : value
 */
async function getKV(key): Promise<any> {
    const data = await getKVs([key])
    if (data !== null && key in data) return data[key]
    return null
}

/**
 * @description 获取单条推文及翻译，以数据库结构
 * @param tid 推文id
 * @returns { db_twitter, db_user, db_translation }
 */
async function get(tid: number): Promise<dbtw> {
    const ret = await rest("/api/db/task/get", { tid })
    if (!ret) return null
    return ret
}

/**
 * @description 获取单条推文及翻译
 * @param tid 推文id
 * @returns Twitter
 */
async function getTask(tid: number): Promise<Twitter> {
    const tw: dbtw = await get(tid)
    if (tw) {
        if (tw.twitter.refTid && !tw.twitter.url && !tw.twitter.content) {
            const ref = await get(tw.twitter.refTid)
            if (ref) {
                return convert(ref, tw)
            }
        }
        return convert(tw)
    }
    return null
}

/**
 * @description 更新推文注释
 * @param tid 推文id
 * @param comment 注释
 */
function comment(tid: number, comment: string): Promise<dbtw> {
    return rest("/api/db/task/comment", { tid, comment })
}

/**
 * @description 更新推文翻译
 * @param tid 推文id
 * @param trans 翻译
 * @param img 烤推机输出图片地址
 */
function trans(tid: number, trans: string, img: string): Promise<dbtw> {
    lastTrans = tid
    // const res: { twitter: db_twitter, translation: db_translation } = await 
    return rest("/api/db/task/translate", { tid, img, trans })
    // return convert(res.twitter, res.translation, orig)
}

/**
 * @description 获取任务队列头
 * @returns todo || 0
 */
function getTodo(): number {
    return todo
}

/**
 * @description 设置队列头
 * @param tid 推文id 
 */
function setTodo(tid: number): Promise<void> {
    todo = tid
    return setKV("todo", tid.toString())
}

/**
 * @description 获取上一次监视到的烤推发布的推文id
 * @returns catched tid
 * @deprecated 暂未启用
 */
async function getCatch(): Promise<number> {
    const v = parseInt(await getKV("catch"))
    if (!isNaN(v)) return v
    return NaN
}

/**
 * @description 获取最新一条推文的id，不包括隐藏的推
 * @returns newest tid
 */
async function getLastTid(): Promise<number> {
    const tw: dbtw = await rest("/api/db/task/last", { withTranslation: true })
    return tw ? tw.twitter.tid : null
}

/**
 * @description 获取最新一条推文，包括隐藏的推
 * @returns newest tid
 * @deprecated 暂未启用
 */
async function getActualLast(): Promise<Twitter> {
    const tw: dbtw = await rest("/api/db/task/actuallast")
    return convert(tw)
}

/**
 * @description 删除推文
 * @param tid 要删除的推文id
 * @returns 删除成功 ? true : false
 * @deprecated 暂未启用
 */
function deleteTask(tid: number): Promise<boolean> {
    return rest("/api/db/task/delete", { tid })
}

/**
 * @description 列出队列里的推文及翻译，不包括隐藏的推文，格式错误的推文被忽略
 * @param tid 队列头tid
 * @returns Twitter[]
 */
async function list(tid?: number): Promise<Twitter[]> {
    const todo: number = (tid === null || isNaN(tid)) ? getTodo() : tid
    const list: dbtw[] = await rest("/api/db/task/list", { "tid": todo })
    if (list === null) return null
    logger.debug("Got %d Twitter", list.length)
    let result: Twitter[] = [];
    for (const i of list) {
        try {
            result.push(convert(i))
            logger.debug("tid %d: %s", i.twitter.tid, i.twitter.comment || i.translation ? i.translation.translation : i.twitter.content)
        } catch (e) {
            logger.warn("convent twitter error: " + e)
        }
    }
    return result
}

/**
 * @description 隐藏或显示推文
 * 
 * 若推文是隐藏的，则显示它，若推文未隐藏，则隐藏它
 * @param tid 推文id
 */
async function hide(tid: number): Promise<boolean> {
    let tw: dbtw = await get(tid)
    if (!tw) return null
    if (tw.twitter.hided) tw = await rest("/api/db/task/visible", { tid })
    else tw = await rest("/api/db/task/hide", { tid })
    return tw ? tw.twitter.hided : null
}

/**
 * @description 隐藏队列里所有已发布的推文
 */
async function hideAll(): Promise<number[]> {
    const queue: Twitter[] = await list()
    let res: number[] = []
    for (const i of queue) {
        if (i.published) {
            rest("/api/db/task/hide", { tid: i.id })
            res.push(i.id)
        }
    }
    return res
}

/**
 * @description 设置推文状态为已发布
 * @param tid 推文id
 */
async function setPublish(tid: number): Promise<boolean> {
    const tw: dbtw = await rest("/api/db/task/published", { tid })
    return tw ? tw.twitter.published : null
}

/**
 * @description 设置推文状态为未发布
 * @param tid 推文id
 */
async function setUnpublish(tid: number): Promise<boolean> {
    const tw: dbtw = await rest("/api/db/task/unpublished", { tid })
    return tw ? tw.twitter.published : null
}

/**
 * @description 获取最后修改翻译的推文id
 * 
 * 仅使用运行时（内存）储存，重启应用丢失
 * @returns 最后一次翻译的推文id
 */
function getLastTrans(): number {
    return lastTrans
}

/**
 * @description 获取某条推文的全部翻译版本，因为db有undo实现了所以不再需要
 * @param tid 推文id
 * @deprecated 暂未使用
 */
function getallTrans(tid: number): Promise<any> {
    return rest("/api/db/task/translations", { tid })
}

/**
 * @description 撤销一条推文的翻译修改
 * @param tid 推文id
 * @returns 推文及当前翻译
 */
async function undo(tid: number): Promise<Twitter> {
    const tw: dbtw = await rest("/api/db/task/rollback", { tid })
    return convert(tw)
}

async function init(ctx: Context, Host: string) {
    logger = ctx.logger("app:store")          // 初始化logger
    host = Host                               // 初始化DB的Host
    todo = parseInt(await getKV("todo")) || 0 // 初始化队列头
    logger.info("store client ready")
}

export default {
    init,
    getTask,
    comment,
    trans,
    getTodo,
    setTodo,
    getLastTid,
    setPublish,
    setUnpublish,
    list,
    hide,
    hideAll,
    getLastTrans,
    undo
}