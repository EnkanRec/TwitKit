import { Context, Logger, Meta } from 'koishi-core'
import { Twitter, Twitter2msg } from './twitter'
import store from './store'
import waitress from './waitress'
import translator from './translator'
import { config, config_cmd } from './utils'

/**
 * @member Map<群号, 群成员Q号[]>
 * @description 储存各群群成员，仅用来更新members
 */
let groups: Map<number, number[]> = new Map()
/**
 * @description 储存允许私聊上班的Q号
 */
let members: Set<number> = new Set()
/**
 * @description 是否允许使用命令
 * @global
 */
let promission: boolean = false

export default function (ctx: Context, argv: config) {
    const cmd: config_cmd = {
        host: {
            store:      argv.cmd.host.store     || "http://localhost",
            waitress:   argv.cmd.host.waitress  || "http://localhost",
            translator: argv.cmd.host.translator|| "http://localhost"
        },
        cut:    argv.cmd.cut    || 8,
        group:  argv.cmd.group  || [],
        friend :argv.cmd.friend || false,
        private:argv.cmd.private|| false
    }
    translator.init(ctx, cmd.host.translator)
    store.init(ctx, cmd.host.store)
    waitress.init(ctx, cmd.host.waitress)
    const logger: Logger = ctx.logger("app:cmd")
    // 初始化群成员列表
    if (cmd.group.length && cmd.private) {
        for (const i of cmd.group) groups.set(i, [])
        const updateMember = async (meta: Meta<"notice">) => {
            if (meta.groupId && groups.has(meta.groupId)) {
                try {
                    const list = await ctx.sender.getGroupMemberList(meta.groupId)
                    groups.set(meta.groupId, list.map<number>((i) => { return i.userId }))
                } catch (e) {
                    logger.warn("get group member fail: %d", meta.groupId)
                    logger.debug(e)
                    groups.set(meta.groupId, [])
                }
                members.clear()
                groups.forEach((v) => { v.forEach((i) => { members.add(i) }) })
                logger.debug(members)
            }
        }
        // 监视群成员变动
        ctx.receiver.on("group-increase", updateMember)
        ctx.receiver.on("group-decrease", updateMember)
        // 启动时初始化群成员
        ctx.receiver.on("connect", () => {
            members.clear()
            groups.forEach(async (v, k) => { // 由于异步，使用forEach将导致无法输出members的log
                try {
                    // 新加入的群getGroupMemberList可能失败
                    const list = await ctx.sender.getGroupMemberList(k)
                    groups.set(k, list.map<number>((i) => { return i.userId }))
                    list.forEach((i) => { members.add(i.userId) })
                } catch (e) {
                    logger.warn("get group member fail: %d", k)
                    logger.debug(e)
                    groups.set(k, [])
                }
            })
            // logger.debug(members)
        })
    }
    // 前置中间件判断权限
    ctx.prependMiddleware((meta, next) => {
        switch (meta.messageType) {
            case "private":
                switch (meta.subType) {
                    case "friend":
                        // 配置好友私聊上班
                        if (cmd.friend) break
                        // 好友不允许上班，继续判断是否是群成员，case穿越
                    case "group":
                        // 配置不许私聊上班
                        if (!cmd.private) {
                            logger.debug("Ignore private message")
                            promission = false
                            return next()
                        }
                        // 如何群列表为空则允许所有人上班
                        if (!cmd.group.length) break
                        // 指定群的成员允许临时会话私聊上班
                        if (members.has(meta.userId)) break
                    default:
                        // 其他通通不给私聊上班
                        logger.debug("Ignore unknown private")
                        promission = false
                        return next()
                }
                break
            case "discuss":
                // 似乎没有必要讨论组上班
                // if (cmd.group.indexOf(meta.discussId)) break
                // 讨论组升级成群？
            case "group":
                // cmd.group为空则允许所有群上班
                if (!cmd.group.length) break
                // 检测是否是允许的群
                if (meta.groupId && ~cmd.group.indexOf(meta.groupId)) break
            default:
                logger.debug("Ignore illegal source")
                promission = false
                return next()
        }
        promission = true
        logger.debug("allow command")
        return next()
    })
    // 中间件解析短快捷指令
    ctx.middleware((meta, next) => {
        if (meta.message.startsWith(argv.prefix)) {
            const msg = meta.message.slice(argv.prefix.length)
            const r = /^(\d+)(~~|[^\d\s])?\s*([\s\S]*)$/.exec(msg)
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
                    case "!":
                    case "！":
                        return ctx.runCommand('fresh', meta, [twi])
                    case "*":
                        return ctx.runCommand('raw', meta, [twi])
                    case "~":
                        return ctx.runCommand('list', meta, [twi])
                    case "~~":
                        return ctx.runCommand('list-detail', meta, [twi])
                    case "/":
                        return ctx.runCommand('clear', meta, [twi])
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
                    case "!":
                    case "！":
                        return ctx.runCommand('fresh', meta)
                    case "*":
                        return ctx.runCommand('raw', meta,)
                    case "~~":
                        return ctx.runCommand('list-detail', meta)
                    case "/":
                        return ctx.runCommand('clear', meta)
                    case "-":
                        return ctx.runCommand('hide', meta)
                    case "?":
                    case "？":
                        return meta.$send("快捷命令使用帮助: " + argv.prefix + "[id][cmd]\n"
                                        + "id: 任务id\n"
                                        + "cmd: 短指令\n"
                                        + "    !: 刷新任务烤图\n"
                                        + "    *: 显示推文原文\n"
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
                        const r = /^(https?:\/\/((www\.)?twitter\.com|t\.co)[\/\w]+)\s*([\s\S]*)$/.exec(msg)
                        if (r)
                            return ctx.runCommand('fetch', meta, [r[1], r[4]])
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
            if (!promission) return
            const twi = parseInt(id)
            trans = trans.replace(/\[CQ:[^\]]*\]/g, " ").trim()
            if (isNaN(twi)) {
                logger.warn("Bad translate id: " + id)
                return meta.$send("找不到 " + id)
            } else {
                let tw: Twitter = await store.getTask(twi)
                if (!tw) {
                    logger.warn("Twitter %d not found", twi)
                    return meta.$send("找不到 " + id)
                }
                if (trans) {
                    if (tw.type === "转推") {
                        await store.trans(tw.refTid, trans, "")
                        logger.debug("update ref twitter translation: %d", tw.refTid)
                        tw.img = await translator.get(tw)
                        await store.trans(twi, "", tw.img)
                        logger.debug("update twitter image: %d", twi)
                    } else {
                        tw.trans = trans
                        tw.img = await translator.get(tw)
                        store.trans(twi, trans, tw.img)
                        logger.debug("update translation: " + trans)
                    }
                }
                if (tw.trans || tw.img) {
                    if (!tw.img) {
                        tw.img = await translator.get(tw)
                        // store.undo(tw.id)
                        store.trans(twi, tw.trans, tw.img, false)
                    }
                    logger.debug("Show translation: " + tw.img)
                    return meta.$send(tw.img + (argv.ispro ? "\n[CQ:image,cache=0,file=" + tw.img + "]" : ""))
                } else {
                    logger.debug("Show raw Twitter: " + tw.content)
                    const msg = Twitter2msg(tw, argv)
                    return meta.$send(msg)
                }
            }
        })
        .usage("获取/更新这个id的翻译内容")

    ctx.command('fetch <url> [trans...]')
        .action(async ({ meta }, url, trans) => {
            if (!promission) return
            if (/^https?:\/\/((www\.)?twitter\.com|t\.co)\//.test(url)) {
                trans = trans.replace(/\[CQ:[^\]]*\]/g, " ").trim()
                if (trans) {
                    logger.debug("translate with url=%s trans=%s", url, trans)
                    const img = await translator.getByUrl(url, trans)
                    return meta.$send(img + (argv.ispro ? "\n[CQ:image,cache=0,file=" + img + "]" : ""))
                } else {
                    logger.debug("fetch new task: %s", url)
                    let list = (await waitress.addTask(url)).sort().reverse()
                    let ref: number[] = []
                    for (const i of list) {
                        if (~ref.indexOf(i)) {
                            logger.debug("ignore ref tid: %d", i)
                            continue
                        }
                        const tw: Twitter = await store.getTask(i)
                        if (tw.type === "转推") ref.push(tw.refTid)
                        logger.debug("[" + tw.user.display + "]" + tw.content)
                        const msg = Twitter2msg(tw, argv)
                        meta.$send(msg)
                    }
                }
            } else {
                logger.debug("unsupport url: " + url)
                meta.$send("不支持的链接")
            }
        })
        .usage("将指定链接的推文入库或直接烤图")

    ctx.command('fresh [id]')
        .action(async ({ meta }, id) => {
            if (!promission) return
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getLastTrans()
            }
            let tw: Twitter = await store.getTask(twi)
            if (!tw) {
                logger.warn("Twitter %d not found", twi)
                return meta.$send("找不到 " + id)
            }
            // if (tw.type === "更新" && !tw.trans) return meta.$send(argv.prefix + id + " 还没有翻译")
            tw.img = await translator.get(tw)
            store.trans(twi, tw.trans, tw.img, false)
            logger.debug("Update image: " + tw.img)
            return meta.$send(tw.img + (argv.ispro ? "\n[CQ:image,cache=0,file=" + tw.img + "]" : ""))
        })
        .usage("刷新这个id的翻译烤图")

    ctx.command('raw [id]')
        .action(async ({ meta }, id) => {
            if (!promission) return
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = store.getLastTrans()
            }
            let tw: Twitter = await store.getTask(twi)
            if (!tw) {
                logger.warn("Twitter %d not found", twi)
                return meta.$send("找不到 " + id)
            }
            const msg = Twitter2msg(tw, argv)
            return meta.$send(msg)
        })
        .usage("显示这个id的推文原文")

    ctx.command('list [id]')
        .action(async ({ meta }, id) => {
            if (!promission) return
            const twi = id ? parseInt(id) : store.getTodo()
            logger.debug("show list from %d", twi)
            const list: Twitter[] = await store.list(twi)
            let msg:string
            if (list && list.length) {
                msg = "从" + argv.prefix + twi + "到现在的已烤推特如下: "
                for (const i of list) {
                    msg += "\n" + argv.prefix + i.id
                    if (i.type !== "更新")  {
                        msg += " " + i.type + " " + argv.prefix + i.refTid
                    }
                    if (i.published) msg += " 已发"
                    if (i.trans || i.img) {
                        msg += " 已烤"
                        if (i.comment) msg += "#" + i.comment
                        else msg += "-" + ((i.trans.length > cmd.cut)
                            ? i.trans.substr(0, cmd.cut).replace(/\n/g, ' ') + "…" 
                            : i.trans.replace(/\n/g, ' ')
                        ) // short(i.trans)
                    } else {
                        if (i.comment) msg += "#" + i.comment
                    }
                }
                msg += "\n--------共" + list.length + "条--------\n"
                    + "发送 " + argv.prefix + "推特ID 以获取详细信息\n"
                    + "发送 " + argv.prefix + twi + "~~以批量获取嵌字结果"
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
            if (!promission) return
            const twi = id ? parseInt(id) : store.getTodo()
            logger.debug("show translation from %d", twi)
            const list = await store.list(twi)
            let msg:string = ""
            if (list) for (const i of list) if (i.trans || i.img) {
                msg += "\n" + argv.prefix + i.id + "\n"
                if (i.img) msg += argv.ispro ? "[CQ:image,file=" + i.img + "]" : i.img
                if (i.media && i.media.length) for (const j of i.media)
                    msg += argv.ispro ? "[CQ:image,file=" + j + "]" : j
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

    ctx.command('clear [id]')
        .action(async ({ meta }, id) => {
            if (!promission) return
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
                + "效果等价于" + argv.prefix + twi + "~与" + argv.prefix + twi + "~~"
            )
        })
        .usage("设置队列头，id为空时，设置成最新的id（清空队列）")

    ctx.command('hide [id]')
        .action(async ({ meta }, id) => {
            if (!promission) return
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
                const list = await store.hideAll()
                if (!list.length) {
                    logger.debug("nothing hide")
                    return meta.$send("队列里没有已发布的推")
                }
                logger.debug("hide tasks: " + list.join(", "))
                return meta.$send("以下推文已经隐藏" + argv.prefix + list.join(", " + argv.prefix))
            }
        })
        .usage("隐藏或显示某个推，id为空时，隐藏所有已烤的推")

    ctx.command('comment [id] [comment...]')
        .action(async ({ meta }, id, comment) => {
            if (!promission) return
            let twi = parseInt(id)
            if (isNaN(twi)) {
                twi = await store.getLastTid()
                if (twi === null || isNaN(twi)) {
                    logger.debug("comment nothing")
                    return meta.$send("没有可用任务")
                } else {
                    logger.debug("Id as part of comment")
                    comment = id + " " + comment
                }
            }
            comment = comment.trim()
            const tw = await store.comment(twi, comment)
            if (tw) {
                logger.warn("comment %d : %s", twi, comment)
                return meta.$send("已在 " + argv.prefix + twi + "上添加了备注: " + tw.twitter.comment)
            } else {
                logger.warn("comment %d fail", twi)
                meta.$send("找不到: " + twi)
            }
        })
        .usage("为某个推添加注释，id为空时，加到最近的推")

    ctx.command('undo [id]')
        .action(async ({ meta }, id) => {
            if (!promission) return
            const twi = id ? parseInt(id) : store.getLastTrans()
            if (twi === null || isNaN(twi)) {
                logger.debug("Nothing to undo")
                return meta.$send("找不到推文: " + argv.prefix + id)
            }
            let tw = await store.undo(twi)
            if (tw) {
                logger.debug("undo translate: %d", tw.id)
                if (tw.type === "转推") {
                    const tw2 = await store.undo(tw.refTid)
                    if (!tw2) logger.warn("ref twitter %d undo fail", tw.refTid)
                    else {
                        logger.debug("undo translate: %d", tw2.id)
                        return meta.$send("已撤销 " + argv.prefix + twi + ">" + argv.prefix + tw2.id
                                        + " 的修改，现在的翻译是: \n" + tw2.trans)
                    }
                }
                return meta.$send("已撤销 " + argv.prefix + twi + " 的修改，现在的翻译是: \n" + tw.trans)
            } else {
                logger.warn("Twitter %d notfound", twi)
                return meta.$send("找不到推文: " + argv.prefix + id)
            }
        })
        .usage("撤销某个推的翻译修改，id为空时，撤销最近修改过的翻译，不会撤销初始翻译")
}