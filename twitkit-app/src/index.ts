export const name = 'translator-bot'
import { Context } from 'koishi-core'
import watcher from './watcher'
import cmd from './cmd'
import { config } from './utils'

export {
    watcher
}

export async function apply (ctx: Context, argv?: config) {
    const Logger = ctx.logger("info")
    if (argv.ispro && await ctx.sender.canSendImage() === false) argv.ispro = false
    Logger.debug("apply plugin watcher")
    ctx.plugin(watcher, argv)
    Logger.debug("apply plugin cmd")
    ctx.plugin(cmd, argv)
}