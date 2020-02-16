import { Context, Logger, Meta } from 'koishi-core'
import { Twitter } from './twitter'
import store from './store'
import translator from './translator'
import { config } from './utils'

let logger: Logger
let context: Context
let groups: Array<number[]> = []
let members: Set<number> = new Set()

async function updateMember(meta?: Meta<"notice">) {
    if (meta && meta.groupId) {
        const list = await context.sender.getGroupMemberList(meta.groupId)
        groups[meta.groupId] = list.map<number>((i) => { return i.userId })
    } else {
        for (const i in groups) {
            const list = await context.sender.getGroupMemberList(parseInt(i))
            groups[i] = list.map<number>((i) => { return i.userId })
        }
    }
    members.clear()
    groups.forEach((v) => { v.forEach((i) => { if (i) members.add(i) }) })
}

export default function (ctx: Context, argv: config) {
    translator.init(ctx, argv.host.translator)
    store.init(ctx, argv.host.store)
    logger = ctx.logger("app:cmd")
    context = ctx
    if (argv.listen && argv.listen.length && argv.private) {
        for (const i of argv.listen) {
            groups[i] = []
        }
        ctx.receiver.on("group-increase", updateMember)
        ctx.receiver.on("group-decrease", updateMember)
        ctx.receiver.on("ready", updateMember)
    }
    // 中间件判断权限及解析短快捷指令
    ctx.middleware((meta, next) => {
        if (argv.listen && argv.listen.length) switch (meta.messageType) {
            case "private":
                if (!argv.private) {
                    // 配置不许私聊上班
                    logger.debug("Ignore private message")
                    return next()
                }
                switch (meta.subType) {
                    case "friend":
                        // 好友允许私聊上班
                        break
                    case "group":
                        // 指定群的成员允许临时会话私聊上班
                        if (members.has(meta.userId)) break
                    default:
                        // 其他通通不给私聊上班
                        logger.debug("Ignore unknown private")
                        return next()
                }
                break
            case "group":
                // argv.listen为空则不限制群上班，否则检测是否是允许群
                if (!argv.listen || meta.groupId && meta.groupId in argv.listen) break
            case "discuss":
                // 似乎没有必要讨论组上班
                // if (!(meta.discussId in groups)) return next()
                // break
            default:
                logger.debug("Ignore illegal source")
                return next()
        }
        if (meta.message.startsWith(argv.prefix)) {
            const msg = meta.message.slice(argv.prefix.length)
            const r = /^(\d+)(~~|[^\d\s])?\s*(.*)$/.exec(msg)
            if (r && r[1]) {
                const twi = r[1]
                const act = r[2]
                const trans = r[3]
                logger.debug("Recv a commend with tid=%d act=%s", twi, act)
                if (trans) logger.debug("trans=" + trans)
                switch (act) {
                    case undefined:
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
                        logger.debug("But match nothing.")
                        return next()
                }
            } else {
                logger.debug("Recv a commend without tid; " + msg)
                switch (msg) {
                    case "":
                    case "~":
                        return ctx.runCommand('list', meta)
                    case "~~":
                        return ctx.runCommand('list-detail', meta)
                    case "/":
                        return ctx.runCommand('current', meta)
                    case "-":
                        return ctx.runCommand('hide', meta)
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
                        if (msg[0] === "+")
                            return ctx.runCommand('comment', meta, ["", msg.slice(1)])
                        logger.debug("But match nothing.")
                        return next()
                }
            }
        }
        return next()
    })

    // 命令实现具体功能
    ctx.command('translate <id> [trans...]')
        .action(async ({ meta }, id, trans) => {
            const twi = parseInt(id)
            trans = trans.trim()
            if (isNaN(twi)) {
                if (/^https?:\/\/(((www\.)?twitter\.com)|(t\.co))\//.test(id)) {
                    logger.debug("translate with url=%s trans=%s", id, trans)
                    const img = await translator.getByUrl(id, trans)
                    if (!img) return meta.$send("请求失败")
                    return meta.$send(img + (argv.ispro ? "\n[CQ:image,file=" + img + "]" : ""))
                }
                logger.warn("Bad translate id: " + id)
                return meta.$send("找不到 " + id)
            } else {
                let tw: Twitter = await store.getTask(twi)
                if (!tw) {
                    logger.warn("Twitter %d not found", twi)
                    return meta.$send("找不到 " + id)
                }
                if (trans) {
                    tw.trans = trans
                    tw.img = await translator.get(tw)
                    store.trans(twi, trans, tw.img)
                    logger.debug("update translation: " + trans)
                }
                if (tw.trans) {
                    logger.debug("Show translation: " + tw.img)
                    return meta.$send(tw.img + (argv.ispro ? "\n[CQ:image,file=" + tw.img + "]" : ""))
                } else {
                    logger.debug("Show raw Twitter: " + tw.content)
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
            const twi = id ? parseInt(id) : store.getTodo()
            logger.debug("show list from %d", twi)
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
                logger.debug("Found %d Twitters", list.length)
            } else {
                logger.debug("But nothing found")
                msg = "列表为空"
            }
            return meta.$send(msg)
        })
        .usage("查看队列某个id后的任务，id为空时使用预设的队列头")

    ctx.command('list-detail [id]')
        .action(async ({ meta }, id) => {
            const twi = id ? parseInt(id) : store.getTodo()
            logger.debug("show translation from %d", twi)
            const list = await store.list(twi)
            let msg:string = ""
            if (list) for (const i of list) if (i.trans) {
                msg += "\n" + argv.prefix + i.id + "\n"
                msg += argv.ispro ? "[CQ:image,file=" + i.img + "]" : i.img
            }
            if (msg.length) {
                msg = "从" + argv.prefix + twi + "到现在的已烤推特如下: " + msg
            } else {
                msg = "列表为空"
                logger.debug("But nothing found")
            }
            return meta.$send(msg)
        })
        .usage("批量获取队列某个id后的烤推结果，id为空时使用预设的队列头")

    ctx.command('current [id]')
        .action(async ({ meta }, id) => {
            const todo = store.getTodo()
            logger.debug("Old todo: " + todo)
            const twi = id ? parseInt(id) : await store.getLastTid()
            if (twi === null || isNaN(twi)) {
                logger.warn("Set todo with wrong id: " + id)
                return meta.$send("设置队列头失败")
            }
            store.setTodo(twi)
            logger.debug("change todo from %d to %d", todo, twi)
            return meta.$send(
                "修改前的快速搜索ID为 " + argv.prefix + todo + "\n"
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
                const hide = await store.hide(twi)
                if (hide === null) {
                    logger.warn("hide %s fail", id)
                    return meta.$send("隐藏失败")
                }
                return meta.$send("已经" + (hide ? "隐藏" : "显示") + "推文 " + argv.prefix + id)
            } else {
                logger.debug("hide all published Twitters in queue")
                store.hideAll()
                ctx.runCommand("list", meta)
            }
        })
        .usage("隐藏或显示某个推，id为空时，隐藏所有已烤的推")

    ctx.command('comment [id] [comment...]')
        .action(async ({ meta }, id, comment) => {
            let twi = parseInt(id)
            if (isNaN(twi)) twi = await store.getLastTid()
            if (twi === null || isNaN(twi)) {
                logger.debug("comment nothing")
                return meta.$send("没有可用任务")
            } else {
                logger.debug("Id as part of comment")
                comment = id + " " + comment
            }
            comment = comment.trim()
            const tw = await store.comment(twi, comment)
            if (tw) {
                logger.warn("comment %d : %s", twi, comment)
                return meta.$send("已在 " + argv.prefix + twi + "上添加了备注: " + tw.comment)
            } else {
                logger.warn("comment %d fail", twi)
                meta.$send("找不到: " + twi)
            }
        })
        .usage("为某个推添加注释，id为空时，加到最近的推")

    ctx.command('undo [id]')
        .action(async ({ meta }, id) => {
            const twi = id ? parseInt(id) : store.getLastTrans()
            if (twi === null || isNaN(twi)) {
                logger.debug("Nothing to undo")
                return meta.$send("找不到推文: " + argv.prefix + id)
            }
            const tw = await store.undo(twi)
            if (tw) {
                logger.debug("undo translate: %d", tw)
                let msg:string = "已撤销修改，现在" + argv.prefix + twi + "的翻译是: \n" + tw.trans
                return meta.$send(msg)
            } else {
                logger.warn("Twitter %d notfound", twi)
                return meta.$send("找不到推文: " + argv.prefix + id)
            }
        })
        .usage("撤销某个推的翻译修改，id为空时，撤销最近修改过的翻译，不会撤销初始翻译")

    ctx.command('set-twid <twid>')
        .action(async ({ meta }, twid) => {
            await store.setTwid(twid)
            logger.debug("update Twitter ID: " + twid)
            return meta.$send("更新推主ID成功")
        })
        .usage("设置推主ID，用于确识别转推，只用设置一次")
}