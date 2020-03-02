import { ISO8601 } from './utils'
export class Twitter {
    /** twitter **/
    id:         number
    statusId:   string
    url:        string
    content:    string
    media:      string[]
    published:  boolean
    type:       "更新" | "转推" | "引用"
    postDate?:  string
    refTid:     number
    comment?:   string
    /** translation **/
    trans?:     string
    img?:       string
    /** users **/
    user: {
        twitterUid: string
        name: string
        display: string
        avatar: string
    }
    oirgUser?: {
        twitterUid: string
        name: string
        display: string
        avatar: string
    }
    /** extra **/
    extra?:      any
}

interface db_twitter {
    tid: number
    statusId: string
    url: string
    content: string
    media: string
    published: boolean
    hided: boolean
    comment: string
    twitterUid: string
    refTid: number
    pubDate: ISO8601
    extra: string
    newdate: ISO8601
    updatetime: ISO8601
}

interface db_user {
    uid: number
    twitterUid: string
    name: string
    display: string
    avatar: string
    newdate: ISO8601
    updatetime: ISO8601
}

interface db_translation {
    zzid: number
    version: number
    translation: string
    img: string
    newdate: ISO8601
    updatetime: ISO8601
}
export type dbtw = {
    twitter: db_twitter,
    user: db_user,
    translation?: db_translation
}

function twURL2user(url: string): string {
    const r = /twitter\.com\/([^\/]+)\/.+/.exec(url)
    return r ? r[1] : undefined
}

function makeTwUrl(statusId: string, name: string = "_"): string {
    return "https://twitter.com/" + name + "/status/" + statusId
}

/**
 * @description 将数据库几个表整合成本地类
 * @param dbtw 推文内容
 * @param orig 转发者，非转发推时为空
 */
export function convert(dbtw: dbtw, orig?: dbtw): Twitter {
    let tw: Twitter = {
        id: dbtw.twitter.tid,
        statusId: dbtw.twitter.statusId,
        url: dbtw.twitter.url || makeTwUrl(dbtw.twitter.statusId, dbtw.user.name),
        content: dbtw.twitter.content,
        media: JSON.parse(dbtw.twitter.media),
        published: dbtw.twitter.published,
        type: "更新",
        postDate: dbtw.twitter.pubDate,
        refTid: dbtw.twitter.refTid,
        comment: dbtw.twitter.comment,
        trans: dbtw.translation ? dbtw.translation.translation : undefined,
        img: dbtw.translation ? dbtw.translation.img : undefined,
        user: {
            twitterUid: dbtw.user.twitterUid,
            name: dbtw.user.name,
            display: dbtw.user.display,
            avatar: dbtw.user.avatar
        },
        extra: JSON.parse(dbtw.twitter.extra)
    }
    if (orig) {
        tw.type = "转推"
        tw.oirgUser = {
            twitterUid: orig.user.twitterUid,
            name: orig.user.name,
            display: orig.user.display,
            avatar: orig.user.avatar
        }
    } else if (dbtw.twitter.refTid) {
        tw.type = "引用"
    }
    return tw
}

export function Twitter2msg(tw: Twitter, argv): string {
    let msg: string = "【" + tw.user.display + "】"
        + ((tw.type === "更新" || tw.type === "引用") ? "更新了" : ("转发了【" + tw.oirgUser.display + "】的推" + argv.prefix + tw.refTid))
        + "\n----------------\n"
        + "内容: " + tw.content
    if (tw.media && tw.media.length) {
        msg += "\n媒体: "
        for (const img of tw.media) msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
    }
    if (tw.type === "引用") {
        msg += "\n引用推文: " + argv.prefix + tw.refTid
    }
    msg += "\n原链接: " + tw.url + "\n快速嵌字发送: " + argv.prefix + tw.id + " 译文"
    return msg
}