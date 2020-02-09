import { Context } from 'koishi-core'
import { Twitter } from './twitter'
import store from './store'
import translator from './translator'

export default function (ctx: Context, argv: any = { cut : 8, ispro: true, prefix: '#', host: {} }) {
    translator.init(ctx, argv.host.translator)
    store.init(ctx, argv.host.store)
    let logger = ctx.logger("app:cmd")
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
                    case undefined:
                    case "~":
                        if (msg[1] === '~') 
                            return ctx.runCommand('list-detail', meta)
                        else
                            return ctx.runCommand('list', meta)
                    case "/":
                        return ctx.runCommand('current', meta)
                    case "-":
                        return ctx.runCommand('hide', meta)
                    case "+":
                        return ctx.runCommand('comment', meta, ["", msg.slice(1)])
                    case "?":
                        return meta.$send("快捷命令使用帮助: " + argv.prefix + "[id][cmd]\n"
                                        + "id: 任务id\n"
                                        + "cmd: 短指令\n"
                                        + "    ~: 列出之后的所有任务\n"
                                        + "    ~~: 列出之后的所有烤推结果\n"
                                        + "    /: 设置队列头\n"
                                        + "    -: 隐藏/显示该任务，或批量隐藏队列里已发布的任务\n"
                                        + "    +: 给任务添加备注\n"
                                        + "    ?: 显示这条帮助\n"
                                        + "详细帮助参考bot指令help"
                        )
                    default:
                        return next()
                }
            }
        }
        return next()
    })

    ctx.command('translate <id> [trans...]')
        .action(async ({ meta }, id, trans) => {
            const twi = parseInt(id)
            if (isNaN(twi)) {
                if (/^https?:\/\/(((www\.)?twitter\.com)|(t\.co))\//.test(id)) {
                    const img = await translator.getByUrl(id, trans)
                    if (!img) return meta.$send("请求失败")
                    return meta.$send(img + (argv.ispro ? "\n[CQ:image,file=" + img + "]" : ""))
                }
                return false
            } else {
                let tw: Twitter = await store.getTask(twi)
                if (!tw) {
                    return meta.$send("找不到 " + id)
                }
                if (trans) {
                    tw.trans = trans
                    tw.img = await translator.get(tw)
                    store.trans(twi, trans, tw.img)
                }
                if (tw.trans) {
                    return meta.$send(tw.img + (argv.ispro ? "\n[CQ:image,file=" + tw.img + "]" : ""))
                } else {
                    let msg: string = "【" + tw.user.name + "】" + tw.type
                                    + "\n----------------\n"
                                    + "内容: " + tw.content
                    if (tw.media) {
                        msg += "\n媒体: "
                        for (const img of tw.media) msg += argv.ispro ? "[CQ:image,file=" + img + "]" : img
                    }
                    msg += "\n原链接: " + tw.url + "\n快速嵌字发送: " + argv.prefix + tw.id + " 译文"
                    return meta.$send(msg)
                }
            }
        })
        .usage("获取/更新这个id的翻译内容")

    ctx.command('list [id]')
        .action(async ({ meta }, id) => {
            const twi = id.length ? parseInt(id) : await store.getTodo()
            const list: any[] = await store.list(twi)
            let msg:string
            if (list && list.length) {
                msg = "从" + argv.prefix + twi + "到现在的已烤推特如下: "
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
        .usage("查看队列某个id后的任务，id为空时使用预设的队列头")

    ctx.command('list-detail [id]')
        .action(async ({ meta }, id) => {
            const twi = id.length ? parseInt(id) : await store.getTodo()
            const list = await store.list(twi)
            let msg:string
            for (const i of list) if (i.trans) {
                msg += "\n" + argv.prefix + i.id + "\n"
                msg += argv.ispro ? "[CQ:image,file=" + i.img + "]" : i.img
            }
            if (msg.length) {
                msg = "从" + argv.prefix + twi + "到现在的已烤推特如下: " + msg
            } else {
                msg = "列表为空"
            }
            return meta.$send(msg)
        })
        .usage("批量获取队列某个id后的烤推结果，id为空时使用预设的队列头")

    ctx.command('current [id]')
        .action(async ({ meta }, id) => {
            const todo = store.getTodo()
            let twi = id.length ? parseInt(id) : await store.getLastTid()
            if (isNaN(twi)) {
                return meta.$send("设置队列头失败")
            }
            store.setTodo(twi)
            return meta.$send(
                "修改前的快速搜索ID为 " + argv.prefix + await todo + "\n"
                + argv.prefix + twi + " 已被保存为快速搜索ID\n"
                + "可直接发送" + argv.prefix + "~或" + argv.prefix + "~~\n"
                + "效果等价于" + argv.prefix + twi + "~与" + argv.prefix + twi + "~~\n"
            )
        })
        .usage("设置队列头，id为空时，设置成最新的id（清空队列）")

    ctx.command('hide [id]')
        .action(async ({ meta }, id) => {
            const twi = parseInt(id)
            if (!isNaN(twi)) {
                store.hide(twi)
                return meta.$send("已经隐藏推文 " + argv.prefix + id)
            } else if (id.length === 0) {
                store.hideAll()
                ctx.runCommand("list", meta)
            } else {
                return meta.$send("找不到 " + id)
            }
        })
        .usage("隐藏某个推，id为空时，隐藏所有已烤的推")

    ctx.command('comment [id] [comment...]')
        .action(async ({ meta }, id, comment) => {
            let twi = parseInt(id)
            if (isNaN(twi)) twi = await store.getLastTid()
            if (isNaN(twi)) {
                return meta.$send("没有可用任务")
            } else {
                comment = id + " " + comment
            }
            const tw = await store.comment(twi, comment)
            if (tw) {
                return meta.$send("已在 " + argv.prefix + twi + "上添加了备注: " + tw.comment)
            } else {
                meta.$send("找不到: " + twi)
            }
        })
        .usage("为某个推添加注释，id为空时，加到最近的推")

    ctx.command('undo [id]')
        .action(async ({ meta }, id) => {
            const twi = id.length ? parseInt(id) : store.getLastTrans()
            if (isNaN(twi)) {
                return meta.$send("找不到推文:  " + argv.prefix + id)
            }
            const tw = await store.undo(twi)
            let msg:string = "已撤销修改，现在" + argv.prefix + twi + "的翻译是: \n" + tw.trans
            return meta.$send(msg)
        })
        .usage("撤销某个推的翻译修改，id为空时，撤销最近修改过的翻译，不会撤销初始翻译")

    ctx.command('set-twid <twid>')
        .action(async ({ meta }, twid) => {
            await store.setTwid(twid)
            return meta.$send("更新推主ID成功")
        })
}