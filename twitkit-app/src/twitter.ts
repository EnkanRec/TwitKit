import { ISO8601 } from './utils'
export class Twitter {
    id:     number
    url:    string
    content:string
    media:  string[]
    published: boolean
    type: string
    postDate?: string
    comment?: string
    trans?: string
    img?:   string
    user: {
        orig?: string,
        id: string
    }
}

export class db_twitter {
    tid: number
    url: string
    content: string
    media: string
    published: boolean
    hided: boolean
    comment: string
    newdate: ISO8601
    updatetime: ISO8601
}

export class db_translation {
    zzid: number
    tid: number
    version: number
    translation: string
    img: string
    newdate: ISO8601
    updatetime: ISO8601
}

function twURL2user(url: string): string {
    const r = /twitter\.com\/([^\/]+)\/.+/.exec(url)
    return r ? r[1] : undefined
}

export function convert(dbtw: db_twitter, dbtr?: db_translation, orig?: string): Twitter {
    let tw: Twitter = {
        id: dbtw.tid,
        url: dbtw.url,
        content: dbtw.content,
        media: JSON.parse(dbtw.media),
        published: dbtw.published,
        type: "更新",
        postDate: dbtw.updatetime,
        comment: dbtw.comment,
        trans: dbtr ? dbtr.translation : undefined,
        img: dbtr ? dbtr.img : undefined,
        user: {
            id: twURL2user(dbtw.url),
            orig: orig
        }
    }
    if (orig && tw.user.id !== orig) tw.type = "转推"
    if (!(tw.id && tw.url && tw.content)) throw "broken Twitter data format"
    return tw
}

export function Twitter2msg(tw: Twitter, argv): string {
    let msg: string = "【" + tw.user.orig + "】"
        + ((tw.type === "更新") ? "更新了" : ("转发了" + tw.user.id + "的推"))
        + "\n----------------\n"
        + "内容: " + tw.content
    if (tw.media) {
        msg += "\n媒体: "
        for (const img of tw.media) msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
    }
    msg += "\n原链接: " + tw.url + "\n快速嵌字发送: " + argv.prefix + tw.id + " 译文"
    return msg
}