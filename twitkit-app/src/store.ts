import { Context, Logger } from 'koishi-core'
import { Twitter, db_twitter, db_translation, convert } from './twitter'
import * as utils from './utils'
import axios from 'axios'

let host: string
let logger: Logger
let orig: string
let todo: number = 0
let lastTrans: number

/**
 * 统一包装请求，处理通用错误
 * @param url api路由
 * @param data api数据
 * @returns error ? null : data || true
 */
async function rest(url: string, data?: any): Promise<any> {
    logger.debug("POST " + url)
    logger.debug(data)
    try {
        const res = await axios.post<utils.response>(host + url, new utils.request(data))
        if (res.data.code === 0) {
            logger.debug("Return %d: %s", res.data.code, res.data.msg)
            logger.debug(res.data.data)
            if (typeof res.data.data === "undefined") return true
            return res.data.data
        } else {
            logger.warn("Error %d: %s", res.data.code, res.data.msg)
            return null
        }
    } catch (e) {
        logger.error("Internet error: %d", e.response.status)
        logger.debug(e.response.data)
    }
    return null
}

/**
 * 更新设置
 * @param key 目标设置项
 * @param value 值
 */
function setKV(key: string, value: string): Promise<void> {
    let data: any = {}
    data[key] = value
    return rest("/api/db/kv/set", data)
}

/**
 * 获取多个设置
 * @param keys 所有目标设置
 * @returns value[]
 */
function getKVs(keys: string[]): Promise<any> {
    return rest("/api/db/kv/get", keys)
}

/**
 * 获取单个设置
 * @param key 目标设置项
 * @returns error ? null : value
 */
async function getKV(key): Promise<any> {
    const data: string[] = await getKVs([key])
    if (data !== null && key in data) return data[key]
    return null
}

/**
 * 获取单条推文及翻译，以数据库结构
 * @param tid 推文id
 * @returns { db_twitter, db_translation }
 */
async function get(tid: number): Promise<{ twitter: db_twitter, translation: db_translation }> {
    const ret = await rest("/api/db/task/get", { tid })
    if (!ret) return null
    return ret
}

/**
 * 获取单条推文及翻译
 * @param tid 推文id
 * @returns Twitter
 */
async function getTask(tid: number): Promise<Twitter> {
    const res: { twitter: db_twitter, translation: db_translation } = await get(tid)
    return res ? convert(res.twitter, res.translation, orig) : null
}

/**
 * 更新推文注释
 * @param tid 推文id
 * @param comment 注释
 */
function comment(tid: number, comment: string): Promise<db_twitter> {
    return rest("/api/db/task/comment", { tid, comment })
}

/**
 * 更新推文翻译
 * @param tid 推文id
 * @param trans 翻译
 * @param img 烤推机输出图片地址
 */
function trans(tid: number, trans: string, img: string): Promise<void> {
    lastTrans = tid
    // const res: { twitter: db_twitter, translation: db_translation } = await 
    return rest("/api/db/task/translate", { tid, img, trans })
    // return convert(res.twitter, res.translation, orig)
}

/**
 * 获取任务队列头
 * @returns todo || 0
 */
function getTodo(): number {
    return todo
}

/**
 * 设置队列头
 * @param tid 推文id 
 */
function setTodo(tid: number): Promise<void> {
    todo = tid
    return setKV("todo", tid.toString())
}

/**
 * 设置监视的Twitter账号id，但不影响监视器行为
 * 仅需部署时设置一次
 * 用于确定推文类型（发表/转发）
 * @param twid Twitter ID
 */
async function setTwid(twid: string): Promise<void> {
    await setKV("twid", twid)
    orig = twid
    return
}

/**
 * 获取上一次监视到的烤推发布的推文id
 * @returns catched tid
 * 暂未启用
 */
async function getCatch(): Promise<number> {
    const v = parseInt(await getKV("catch"))
    if (!isNaN(v)) return v
    return NaN
}

/**
 * 获取最新一条推文及翻译
 * 不包括隐藏的推
 * @returns newest tid
 */
async function getLastTid(): Promise<number> {
    const res: { twitter: db_twitter, translation: db_translation } = await rest("/api/db/task/last", { withTranslation: true })
    return res ? res.twitter.tid : null
}

/**
 * 获取最新一条推文
 * 包括隐藏的推
 * @returns newest tid
 * 暂未启用
 */
async function getActualLast(): Promise<Twitter> {
    const dbtw:db_twitter = await rest("/api/db/task/actuallast")
    return convert(dbtw, null, orig)
}

/**
 * 删除推文
 * @param tid 要删除的推文id
 * @returns 删除成功 ? true : false
 * 暂未启用 
 */
function deleteTask(tid: number): Promise<boolean> {
    return rest("/api/db/task/delete", { tid })
}

/**
 * 列出队列里的推文及翻译
 * 不包括隐藏的推文
 * 格式错误的推文被忽略
 * @param tid 队列头tid
 * @returns Twitter[]
 */
async function list(tid?: number): Promise<Twitter[]> {
    const todo: number = (tid === null || isNaN(tid)) ? getTodo() : tid
    const list: { twitter: db_twitter, translation: db_translation }[] = await rest("/api/db/task/list", { "tid": todo })
    if (list === null) return null
    logger.debug("Got %d Twitter", list.length)
    let result: Twitter[] = [];
    for (const i of list) {
        try {
            result.push(convert(i.twitter, i.translation, orig))
            logger.debug("tid %d: %s", i.twitter.tid, i.twitter.comment || i.translation.translation || i.twitter.content)
        } catch (e) {
            logger.warn("convent twitter error: " + e)
        }
    }
    return result
}

/**
 * 隐藏或显示推文
 * 若推文是隐藏的，则显示它
 * 若推文未隐藏，则隐藏它
 * @param tid 推文id
 */
async function hide(tid: number): Promise<boolean> {
    const t = await get(tid)
    if (!t) return null
    let tw = t.twitter
    if (tw.hided) tw = await rest("/api/db/task/visible", { tid })
    else tw = await rest("/api/db/task/hide", { tid })
    return tw ? tw.hided : null
}

/**
 * 隐藏队列里所有已发布的推文
 */
async function hideAll() {
    // const list: { twitter: db_twitter, translation: db_translation }[] = await rest("/api/db/task/list", { "tid": todo })
    // for (const i of list) if (i.twitter.published) rest("/api/db/task/hide", { tid: i.id })
    const todo: Twitter[] = await list()
    for (const i of todo) if (i.published) rest("/api/db/task/hide", { tid: i.id })
}

/**
 * 设置推文状态为已发布
 * @param tid 推文id
 */
function setPublish(tid: number): Promise<void> {
    return rest("/api/db/task/published", { tid })
}

/**
 * 设置推文状态为未发布
 * @param tid 推文id
 */
function setUnpublish(tid: number): Promise<void> {
    return rest("/api/db/task/unpublished", { tid })
}

/**
 * 获取最后修改翻译的推文id
 * 仅使用运行时（内存）储存，重启应用丢失
 * @returns 最后一次翻译的推文id
 */
function getLastTrans(): number {
    return lastTrans
}

/**
 * 获取某条推文的全部翻译版本
 * 因为db有undo实现了所以不再需要
 * @param tid 推文id
 * 暂未使用
 */
function getallTrans(tid: number): Promise<any> {
    return rest("/api/db/task/translations", { tid })
}

/**
 * 撤销一条推文的翻译修改
 * @param tid 推文id
 * @returns 推文及当前翻译
 */
async function undo(tid: number): Promise<Twitter> {
    const res: { twitter: db_twitter, translation: db_translation } = await rest("/api/db/task/rollback", { tid })
    return convert(res.twitter, res.translation, orig)
}

async function init(ctx: Context, Host: string) {
    logger = ctx.logger("app:store")          // 初始化logger
    host = Host || "http://localhost"         // 初始化DB的Host
    orig = await getKV("twid")                // 初始化监视Twitter用户ID
    todo = parseInt(await getKV("todo")) || 0 // 初始化队列头
    if (orig) return logger.debug("store client ready")
    return logger.error("init DB fail")
}

export default {
    init,
    getTask,
    comment,
    trans,
    getTodo,
    setTodo,
    setTwid,
    getLastTid,
    setPublish,
    setUnpublish,
    list,
    hide,
    hideAll,
    getLastTrans,
    undo
}