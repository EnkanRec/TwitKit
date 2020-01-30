import { Twitter } from './twitter'
import * as utils from './utils'
import axios from 'axios'

const host:string = "http://localhost"

async function rest(url: string, data?: any): Promise<any> {
    const res = await axios.post(host + url, new utils.request(data))
    try {
        const resp: utils.response = res.data
        if (!resp.code) {
            return false
        }
        if (resp.data) return resp.data
    } catch (e) {
        return false
    }
    return true
}

function getTask(tid: number): Promise<Twitter> {
    return rest("/api/db/task/get", tid)
}

function setKV(key: string, value: string): Promise<boolean> {
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

async function get(twi: number): Promise<Twitter> {
    return
}

function comment(twi: number, comment: string): Promise<void> {
    return
}

function trans(twi: number, trans: string, img: string): Promise<void> {
    return
}

async function getTodo(): Promise<number> {
    const v = parseInt(await getKV("todo"))
    if (!isNaN(v)) return v
    return NaN
}

function setTodo(twi: number): Promise<boolean> {
    return setKV("todo", twi.toString())
}

async function getCatch(): Promise<number> {
    const v = parseInt(await getKV("catch"))
    if (!isNaN(v)) return v
    return NaN
}

function getlast(): Promise<Twitter> {
    return
}

function list(twi: number): Promise<Twitter[]> {
    return
}

function hide(twi: number[]): Promise<void> {
    return
}

function getlastTrans(): Promise<number> {
    return
}

function getallTrans(twi: number): Promise<number[]> {
    return
}

function undo(twi: number): Promise<void> {
    return
}

export default {
    get,
    comment,
    trans,
    getTodo,
    setTodo,
    getCatch,
    getlast,
    list,
    hide,
    getlastTrans,
    getallTrans,
    undo
}