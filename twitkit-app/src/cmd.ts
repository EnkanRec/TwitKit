import { Context } from 'koishi-core'
import { Twitter } from './twitter'
import store from './store'
import translator from './translator'

export function apply (ctx: Context, argv: any = { cut : 8, ispro: true, prefix: '#' }) {
    ctx.middleware((meta, next) => {
        if (meta.message.startsWith(argv.prefix)) {
            const msg = meta.message.slice(argv.prefix.length)
            const r = /^(d+)(~|~~|\/)?\s*(.*)$/.exec(msg)
            if (r && r[1]) {
                const twi = r[1]
                const act = r[2]
                const trans = r[3]
                switch (act) {
                    case "":
                        return ctx.runCommand('translate', meta, [twi, trans])
                    case "~":
                        return ctx.runCommand('list', meta, [twi])
                    case "~~":
                        return ctx.runCommand('list-detail', meta, [twi])
                    case "/":
                        return ctx.runCommand('current', meta, [twi])
                    case "-":
                        return ctx.runCommand('hide', meta, [twi])
                    case "+":
                        return ctx.runCommand('comment', meta, [twi, trans])
                    default:
                        return next()
                }
            } else {
                switch (msg[0]) {
                    case "":
                    case "~":
                        if (msg.length > 1 && msg[1] === '~') 
                            return ctx.runCommand('list-detail', meta)
                        else
                            return ctx.runCommand('list', meta)
                    case "/":
                        return ctx.runCommand('current', meta)
                    case "-":
                        return ctx.runCommand('hide', meta)
                    case "+":
                        return ctx.runCommand('comment', meta, ["", msg.slice(1)])
                    default:
                        return next()
                }
            }
        }
        return next()
    })

    ctx.command('translate <id> [trans...]')
        .action(({ meta }, id, trans) => {
            const twi = parseInt(id)
            if (isNaN(twi)) {
                if (/^https?:\/\/(((www\.)?twitter\.com)|(t\.co))\//.test(id)) {
                    const img = translator.getByUrl(id, trans)
                    return meta.$send(img + (argv.ispro ? "\n[CQ:image,file=" + img + "]" : ""))
                }
                return false
            } else {
                let tw: Twitter = store.get(twi)
                if (!tw) {
                    return meta.$send("找不到 " + id)
                }
                if (trans) {
                    tw.trans = trans
                    tw.img = translator.get(tw)
                    store.update(twi, trans, tw.img)
                }
                if (tw.trans) {
                    return meta.$send(tw.img + (argv.ispro ? "\n[CQ:image,file=" + tw.img + "]" : ""))
                } else {
                    let msg: string = "【" + tw.user.name + "】" + tw.type
                                    + "\n----------------\n"
                                    + "内容：" + tw.content
                    if (tw.media) {
                        msg += "\n媒体："
                        for (const img of tw.media) msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
                    }
                    msg += "\n原链接：" + tw.url + "\n快速嵌字发送：" + argv.prefix + tw.id + " 译文"
                    return meta.$send(msg)
                }
            }
        })
        .usage("获取/更新这个id的翻译内容")

    ctx.command('list [id]')
        .action(({ meta }, id) => {
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getTodo()
            }
            const list: any[] = store.list(twi)
            let msg:string
            if (list.length) {
                msg = "从" + argv.prefix + twi + "到现在的已烤推特如下："
                for (const i of list) {
                    msg += "\n" + argv.prefix + i.id
                    if (i.type !== "发布") msg += " " + i.type
                    if (i.published) msg += " 已发"
                    if (i.trans) {
                        msg += " 已烤"
                        if (i.comment) msg += "#" + i.comment
                        else msg += "-" + ((i.trans.length > argv.cut) ? i.trans.substr(0, argv.cut) + "…" : i.trans) // short(i.trans)
                    } else {
                        if (i.comment) msg += "#" + i.comment
                    }
                }
                msg += "\n--------共" + list.length + "条--------\n"
                    + "发送#推特ID以获取详细信息\n"
                    + "发送" + argv.prefix + twi + "~~以批量获取嵌字结果"
            } else {
                msg = "列表为空"
            }
            return meta.$send(msg)
        })
        .usage("查看队列某个id后的任务，id为空时表示快速搜索")

    ctx.command('list-detail [id]')
        .action(({ meta }, id) => {
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getTodo()
            }
            const list: any[] = store.list(twi)
            let msg:string
            for (const i of list) if (i.trans) {
                msg += "\n" + argv.prefix + i.id + "\n"
                msg += argv.ispro ? "[CQ:image,file=" + i.img + "]" : i.img
            }
            if (msg.length) {
                msg = "从" + argv.prefix + twi + "到现在的已烤推特如下：" + msg
            } else {
                msg = "列表为空"
            }
            return meta.$send(msg)
        })
        .usage("批量获取队列某个id后的烤推结果，id为空时使用快速搜索")

    ctx.command('current [id]')
        .action(({ meta }, id) => {
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getCatch()
            }
            store.setTodo(twi)
            return meta.$send("")
        })
        .usage("设置队列头，id为空时，尝试从监控到最后的烤推发布读取")

    ctx.command('hide [id]')
        .action(({ meta }, id) => {
            const twi = parseInt(id)
            let arr: number[] = []
            if (isNaN(twi)) {
                const list: any[] = store.list(twi)
                for (const i of list) if (i.published) arr.push(i.id)
                store.hide(arr)
            } else {
                arr.push(twi)
            }
            store.hide(arr)
            return meta.$send("已经隐藏以下推文： " + argv.prefix + arr.join(" " + argv.prefix))
        })
        .usage("隐藏某个推，id为空时，隐藏所有已烤的推")

    ctx.command('comment [id] [comment...]')
        .action(({ meta }, id, comment) => {
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getlast().id
                comment = id + " " + comment
            }
            store.update(twi, comment=comment)
            meta.$send("")
        })
        .usage("为某个推添加注释，id为空时，加到最近的推")

    ctx.command('undo [id] [rid]')
        .action(({ meta }, id, rid) => {
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getlastTrans()
            }
            const trans: any[] = store.getallTrans(twi)
            if (trans.length < 2) return meta.$send("无可撤销")
            store.undo(trans[trans.length - 1].rid)

            let msg:string = "已撤销修改，现在" + argv.prefix + twi + "的翻译是：\n"
            msg += argv.ispro ? ("[CQ:image,file=" + trans[trans.length - 2].img + "]") : (trans[trans.length - 2].img)
            return meta.$send(msg)
        })
        .usage("撤销某个推的翻译修改，id为空时，撤销最近修改过的翻译，不会撤销初始翻译")
}