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
        name?: string,
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
        type: "发布",
        postDate: dbtw.updatetime,
        comment: dbtw.comment,
        trans: dbtr ? dbtr.translation : undefined,
        img: dbtr.img,
        user: {
            id: twURL2user(dbtw.url)
        }
    }
    if (orig && tw.user.id !== orig) tw.type = "转发"
    if (tw.id && tw.url && tw.content) throw "broken Twitter data format"
    return tw
}