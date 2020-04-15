import { Context, Logger, Meta } from 'koishi-core'
import { CQCode } from 'koishi-utils'
import { Twitter, Twitter2msg, tids2msgs } from './twitter'
import store from './store'
import maid from './maid'
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
            maid:   argv.cmd.host.maid  || "http://localhost",
            translator: argv.cmd.host.translator|| "http://localhost"
        },
        cut:    argv.cmd.cut    || 8,
        group:  argv.cmd.group  || [],
        friend :argv.cmd.friend || false,
        private:argv.cmd.private|| false
    }
    translator.init(ctx, cmd.host.translator)
    store.init(ctx, cmd.host.store)
    maid.init(ctx, cmd.host.maid)
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
                                        + "快捷命令将映射到一个长命令，使用“how”命令查看TwitKit的命令帮助\n"
                                        + "也可以使用Koishi自带的help命令查看帮助\n"
                                        + "群聊可以通过 “@我+指令名”或“" + argv.prefix + "+指令名” 的方式进行使用"
                        )
                    default:
                        if (msg[0] === "+")
                            return ctx.runCommand('comment', meta, ["", msg.slice(1)])
                        const r = /^(https?:\/\/((www\.)?twitter\.com|t\.co)[\/\w!@#%&+-=?~]+)\s*([\s\S]*)$/.exec(msg)
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
    ctx.command('translate <id> [trans...]', "更新(获取)一个任务翻译(图)")
        .option("-d, --hold", "仅更新翻译，不烤图")
        .option("-e, --empty", "允许空翻译，直接烤图")
        .action(async ({ meta, options }, id, trans) => {
            if (!promission) return
            const twi = parseInt(id)
            trans = CQCode.unescape(trans.replace(/\[CQ:[^\]]*\]/g, " ").trim())
            if (isNaN(twi)) {
                logger.warn("Bad translate id: " + id)
                return meta.$send("找不到 " + id)
            } else {
                let tw: Twitter = await store.getTask(twi)
                if (!tw) {
                    logger.warn("Twitter %d not found", twi)
                    return meta.$send("找不到 " + id)
                }
                if (options.d || options.hold) {
                    await store.trans(tw.refTid, trans, "")
                    return meta.$send(argv.prefix + twi + " 已更新翻译：\n" + trans)
                }
                if (trans || options.e || options.empty) {
                    if (tw.type === "转推") {
                        if (trans) await store.trans(tw.refTid, trans, "")
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
        .usage("    id: 推文的任务id\n"
            +  "    trans: 可选，翻译文本\n"
            +  "当trans为空时，若已经有翻译，则返回烤图；否则显示原文(同raw)\n"
            +  "当trans非空时，更新翻译并请求烤图机，返回烤图\n"
            +  "注：翻译文本允许emoji，但不支持QQ表情或图片等元素，不支持的元素会被换成空格"
        )
        .example(argv.prefix + "1000 或 translate 1000 // 显示任务1000的原文或烤图")
        .example(argv.prefix + "1000 <翻译> 或 translate 1000 <翻译> // 翻译任务1000并取得烤图")

    ctx.command('fetch <url> [trans...]', "处理推文链接")
        .action(async ({ meta }, url, trans) => {
            if (!promission) return
            if (/^https?:\/\/((www\.)?twitter\.com|t\.co)\//.test(url)) {
                trans = CQCode.unescape(trans.replace(/\[CQ:[^\]]*\]/g, " ").trim())
                if (trans) {
                    logger.debug("translate with url=%s trans=%s", url, trans)
                    const img = await translator.getByUrl(url, trans)
                    if (!img) return meta.$send("请求失败")
                    return meta.$send(img + (argv.ispro ? "\n[CQ:image,cache=0,file=" + img + "]" : ""))
                } else {
                    logger.debug("fetch new task: %s", url)
                    let list = await maid.addTask(url)
                    if (!list) return meta.$send("请求失败")
                    if (!list.length) return meta.$send("添加失败，是否已经在库中？")
                    let quere: string[] = await tids2msgs(list, argv)
                    for (const msg of quere) await meta.$send(msg)
                }
            } else {
                logger.debug("unsupport url: " + url)
                meta.$send("不支持的链接")
            }
        })
        .usage("    url: 目标推文的url\n"
            +  "    trans: 可选，翻译文本\n"
            +  "当trans为空时，获取推文并入库分配一个任务id，然后显示原文\n"
            +  "当trans非空时，，直接请求烤推机，不会影响库，返回烤图\n"
            +  "注：翻译文本允许emoji，但不支持QQ表情或图片等元素，不支持的元素会被换成空格"
        )
        .example(argv.prefix + "<url> 或 fetch <url> // 将<url>的推文入库，并显示它的原文")
        .example(argv.prefix + "<url> <翻译> 或 fetch <url> <翻译> // 直接对<url>的推文翻译，并返回烤图")

    ctx.command('fresh [id]', "刷新一个翻译图")
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
        .usage("    id: 可选，推文的任务id\n"
            +  "当id为空时，则使用最近一次翻译过的推文\n"
            +  "最近一次翻译过的推文信息仅储存在内存，重启丢弃\n"
            +  "通常在烤推机出错或引用的推文有翻译更新时使用"
        )
        .example(argv.prefix + "! 或 fresh // 刷新最后一次更新过翻译的推文烤图")
        .example(argv.prefix + "1000! 或 fresh 1000 // 刷新任务1000的推文烤图")

    ctx.command('raw [id]', "显示一个任务原文")
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
        .usage("    id: 可选，推文的任务id\n"
            +  "当id为空时，则使用最近一次翻译过的推文\n"
            +  "最近一次翻译过的推文信息仅储存在内存，重启丢弃\n"
            +  "由于在翻译过某个推后，除非undo掉所有的翻译，原文只能以图片形式查看，故留此接口"
        )
        .example(argv.prefix + "* 或 raw // 显示最后一次更新过翻译的推文的原文")
        .example(argv.prefix + "1000* 或 raw 1000 // 显示任务1000的推文原文")

    ctx.command('list [id]', "显示任务队列")
        .option("-d, --detail", "仅输出队列中已翻译的烤图及媒体")
        .action(async ({ meta, options }, id) => {
            if (!promission) return
            const twi = id ? parseInt(id) : store.getTodo()
            logger.debug("show list from %d", twi)
            const list: Twitter[] = await store.list(twi)
            if (!list) {
                logger.debug("Request DB fail")
                return meta.$send("请求失败")
            }
            let task: string[] = []
            if (options.d || options.detail) for (const i of list) {
                if (!(i.trans || i.img)) continue
                let msg = ""
                if (i.img) msg += argv.ispro ? "[CQ:image,file=" + i.img + "]" : i.img
                if (i.media && i.media.length) for (const j of i.media)
                    msg += argv.ispro ? "[CQ:image,file=" + j + "]" : j
                if (i.video && i.video.length) msg += "\n" + i.video.join("\n")
                if (msg) task.push(argv.prefix + i.id + "\n" + msg)
            } else for (const i of list) {
                let msg = argv.prefix + i.id
                if (i.type !== "更新")  {
                    msg += " " + i.type + " " + argv.prefix + i.refTid
                }
                if (i.published) msg += " 已发"
                if (i.trans || i.img) {
                    if (!i.published)msg += " 已烤"
                    if (!i.comment) msg += "-" + (
                        (i.trans.length > cmd.cut)
                        ? i.trans.substr(0, cmd.cut).replace(/\n/g, ' ') + "…"
                        : i.trans.replace(/\n/g, ' ')
                    ) // short(i.trans)
                }
                if (i.comment) msg += "+" + i.comment
                task.push(msg)
            }
            if (!task.length) {
                logger.debug("But nothing found")
                return meta.$send("列表为空")
            }
            let msg = "从" + argv.prefix + twi + "到现在的已烤推特如下:\n"
                + task.join("\n")
                + "\n--------共" + task.length + "条--------\n"
                + "发送 " + argv.prefix + "任务ID 以获取详细信息\n"
                + "发送 " + argv.prefix + twi + "~~以批量获取嵌字结果"
            logger.debug("Found %d Twitters", list.length)
            return meta.$send(msg)
        })
        .usage("显示该任务之后至最新的所有任务简介\n"
            +  "    id: 可选，队列开始前的一条任务id\n"
            +  "当id为空时，则使用队列头，默认为0\n"
            +  "队列头储存在数据库中，并在内存缓存，使用clear命令修改\n"
            +  "队列里不包括作为队列头的任务"
        )
        .example(argv.prefix + " 或 " + argv.prefix + "~ 或 list // 显示当前队列")
        .example(argv.prefix + "1000~ 或 list 1000 // 显示1000之后的所有推文")
        .shortcut("list-detail", { fuzzy: true, options: { detail: true } })
        .example(argv.prefix + "~~ 或 list-detail 或 list --detail // 显示队列中已翻译的烤图及媒体")

    ctx.command('clear [id]', "设置队列头")
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
        .usage("参见list命令\n"
            +  "    id: 可选，设置队列头使用的任务id\n"
            +  "当id为空时，则使用最新一条任务id，相当于清空当前队列"
        )
        .example(argv.prefix + "/ 或 clear // 清空队列")
        .example(argv.prefix + "1000/ 或 clear 1000 // 设置队列头为1000")

    ctx.command('hide [id]', "隐藏或显示任务")
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
        .usage("    id: 可选，操作目标任务id\n"
            +  "当id为空时，则将队列中所有状态为已发的任务隐藏，返回被隐藏的任务id\n"
            +  "当id非空时，则修改该任务隐藏状态，未隐藏则隐藏之，隐藏的显示之"
        )
        .example(argv.prefix + "- 或 hide // 隐藏队列里所有已发的任务")
        .example(argv.prefix + "1000- 或 hide 1000 // 隐藏或显示任务1000")

    ctx.command('comment [id] [comment...]', "给任务添加注释")
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
        .usage("    id: 可选，操作目标任务id\n"
            +  "    comment: 注释内容\n"
            +  "若id不是数字，则会被当成comment的一部分\n"
            +  "当id为空或非数字时，则使用最新一条任务id\n"
            +  "注释会显示在队列里，参见list命令"
        )
        .example(argv.prefix + "+<注释> 或 comment <注释> // 给最新一条任务添加注释")
        .example(argv.prefix + "1000+<注释> 或 comment 1000 <注释> // 给任务1000添加注释")

    ctx.command('undo [id]', "回滚一条翻译")
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
                return meta.$send("已撤销 " + argv.prefix + twi + " 的修改，现在的翻译是: \n" + tw.trans || "<空>")
            } else {
                logger.warn("Twitter %d notfound", twi)
                return meta.$send("找不到推文: " + argv.prefix + id)
            }
        })
        .usage("回滚一个翻译，返回当前翻译文本")
        .usage("    id: 可选，推文的任务id\n"
            +  "当id为空时，则使用最近一次翻译过的推文\n"
            +  "最近一次翻译过的推文信息仅储存在内存，重启丢弃\n"
            +  "这条指令没有设计快捷指令"
        )
        .example("undo // 回滚最近修改过的翻译")
        .example("undo 1000 // 回滚任务1000的翻译")

    ctx.command('delete <id>', "删除一条任务")
        .action(async ({ meta }, id) => {
            if (!promission) return
            const twi = parseInt(id)
            if (twi === null || isNaN(twi)) {
                logger.debug("Nothing to delete")
                return meta.$send("找不到推文: " + argv.prefix + id)
            }
            const stat = await store.deleteTask(twi)
            if (stat) {
                return meta.$send("删除成功")
            } else {
                return meta.$send("删除失败")
            }
        })
        .usage("删除一个任务，返回是否删除成功")
        .usage("    id: 推文的任务id\n"
            +  "一般情况下用不上，删除后任务无法自动恢复\n"
            +  "建议使用hide命令隐藏\n"
            +  "这条指令没有设计快捷指令"
        )
        .example("delete 1000 // 删除任务1000")

    ctx.command('how', "TwitKit的帮助")
        .action(({ meta }) => {
            return meta.$send("命令使用帮助: " + argv.prefix + " <命令> [参数]；括号内为对应的快捷命令\n"
                            + "    " + argv.prefix + " list [id] (" + argv.prefix + "[id]~)\n"
                            + "    查看队列某个id后的任务，id为空时使用预设的队列头\n"
                            + "    " + argv.prefix + " translate <id> [trans] (" + argv.prefix + "[id] [trans])\n"
                            + "    获取/更新这个id的翻译内容\n"
                            + "    " + argv.prefix + " list-detail [id] (" + argv.prefix + "[id]~~)\n"
                            + "    批量获取队列某个id后的烤推结果，id为空时使用预设的队列头\n"
                            + "    " + argv.prefix + " clear [id] (" + argv.prefix + "[id]/)\n"
                            + "    设置队列头，id为空时，设置成最新的id（清空队列）\n"
                            + "    " + argv.prefix + " undo [id]\n"
                            + "    撤销某个推的翻译修改，id为空时，撤销最近修改过的翻译，不会撤销初始翻译\n"
                            + "    " + argv.prefix + " fetch <url> [trans] (" + argv.prefix + "[url] [trans])\n"
                            + "    将指定链接的推文入库或直接烤图\n"
                            + "    " + argv.prefix + " hide [id] (" + argv.prefix + "[id]-)\n"
                            + "    隐藏或显示某个推，id为空时，隐藏所有已烤的推\n"
                            + "    " + argv.prefix + " comment [id] <text> (" + argv.prefix + "[id]+[text])\n"
                            + "    为某个推添加注释，id为空时，加到最近的推\n"
                            + "    " + argv.prefix + " fresh [id] (" + argv.prefix + "[id]!)\n"
                            + "    刷新这个id的翻译烤图，id为空时使用最近修改过的翻译\n"
                            + "    " + argv.prefix + " raw [id] (" + argv.prefix + "[id]*)\n"
                            + "    显示这个id的推文原文，id为空时使用最近修改过的翻译\n"
                            + "另有快捷指令，用 “" + argv.prefix + "?” 查看帮助\n"
                            + "群聊可以通过 “@我+指令名”或“" + argv.prefix + "+指令名” 的方式进行使用\n"
                            + "允许私聊，且私聊不需要添加上述前缀，直接输入指令名即可"
            )
        })
}