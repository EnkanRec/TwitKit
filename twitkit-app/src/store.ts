import { Context, Logger } from 'koishi-core'
import { Twitter, db_twitter, db_translation, convert } from './twitter'
import * as utils from './utils'
import axios from 'axios'
import { promises } from 'dns'

let host: string
let logger: Logger
let orig: string

async function rest(url: string, data?: any): Promise<any> {
    logger.debug("POST " + url)
    logger.debug(data)
    const res = await axios.post<utils.response>(host + url, new utils.request(data))
    if (res.status !== 200) {
        logger.error("Internet error: %d", res.status)
        return null
    }
    if (res.data.code === 0) {
        logger.debug("Return %d: %s", res.data.code, res.data.msg)
        logger.debug(res.data.data)
        if (typeof res.data.data === "undefined") return true
        return res.data.data
    } else {
        logger.warn("Error %d: %s", res.data.code, res.data.msg)
        return null
    }
}

function setKV(key: string, value: string): Promise<void> {
    return rest("/api/db/kv/set", { key: value })
}

function getKVs(keys: string[]): Promise<any> {
    return rest("/api/db/kv/get", keys)
}

async function getKV(key): Promise<any> {
    const data: string[] = await getKVs([key])
    if (key in data) return data[key]
    return null
}

function get(tid: number): Promise<db_twitter> {
    return rest("/api/db/task/get", { tid })
}

async function getTask(tid: number): Promise<Twitter> {
    const dbtw = await get(tid)
    return convert(dbtw, null, orig)
}

function comment(tid: number, comment: string): Promise<void> {
    return rest("/api/db/task/comment", { tid, comment })
}

function trans(tid: number, trans: string, img: string): Promise<void> {
    // const res: { twitter: db_twitter, translation: db_translation} = await 
    return rest("/api/db/task/translate", { tid, img, trans })
    // return convert(res.twitter, res.translation, orig)
}

async function getTodo(): Promise<number> {
    const v = parseInt(await getKV("todo"))
    if (!isNaN(v)) return v
    return 0
}

function setTodo(twi: number): Promise<void> {
    return setKV("todo", twi.toString())
}

async function setTwid(twid: string): Promise<void> {
    await setKV("twid", twid)
    orig = twid
    return
}

async function getCatch(): Promise<number> {
    const v = parseInt(await getKV("catch"))
    if (!isNaN(v)) return v
    return NaN
}

async function getLast(): Promise<Twitter> {
    const res: { twitter: db_twitter, translation: db_translation} = await rest("/api/db/task/last", { withTranslation: true })
    return convert(res.twitter, res.translation, orig)
}

async function getActualLast(): Promise<Twitter> {
    const dbtw:db_twitter = await rest("/api/db/task/actuallast")
    return convert(dbtw, null, orig)
}

function deleteTask(tid: number): Promise<boolean> {
    return rest("/api/db/task/delete", { tid })
}

async function list(twi?: number): Promise<Twitter[]> {
    const todo: number = twi || await getTodo()
    const list: { twitter: db_twitter, translation: db_translation }[] = await rest("/api/db/task/list", { "tid": todo })
    logger.debug("Got %d Twitter", list.length)
    let result: Twitter[] = [];
    for (let i of list) {
        try {
            result.push(convert(i.twitter, i.translation, orig))
            logger.debug("tid %d: %s", i.twitter.tid, i.twitter.comment || i.translation.translation || i.twitter.content)
        } catch (e) {
            logger.warn("convent twitter error: " + e)
        }
    }
    return result
}

async function hide(tid: number): Promise<void> {
    const tw = await get(tid)
    if (tw.hided) return rest("/api/db/task/visible", { tid })
    return rest("/api/db/task/hide", { tid })
}

async function hideAll() {
    const todo: Twitter[] = await list()
    for (const i of todo) if (i.published) rest("/api/db/task/hide", { tid: i.id })
}

function setPublish(tid: number): Promise<void> {
    return rest("/api/db/task/published", { tid })
}

function setUnpublish(tid: number): Promise<void> {
    return rest("/api/db/task/unpublished", { tid })
}

function getlastTrans(): Promise<number> {
    return
}

function getallTrans(tid: number): Promise<any> {
    return rest("/api/db/task/translations", { tid })
}

async function undo(tid: number): Promise<Twitter> {
    const res: { twitter: db_twitter, translation: db_translation} = await rest("/api/db/task/rollback", { tid })
    return convert(res.twitter, res.translation, orig)
}

async function init(ctx: Context, Host: string) {
    logger = ctx.logger("app:translator")
    host = Host || "http://localhost"
    orig = await getKV("twid")
    logger.debug("store client ready")
}

export default {
    init,
    getTask,
    comment,
    trans,
    getTodo,
    setTodo,
    setTwid,
    getLast,
    setPublish,
    setUnpublish,
    list,
    hide,
    hideAll,
    undo
}