export const name = 'translator-bot'
import { Context } from 'koishi-core'
import watcher from './watcher'
import cmd from './cmd'
import { config } from './utils'

export {
    watcher
}

export function apply (ctx: Context, argv?: config) {
    const Logger = ctx.logger("info")
    Logger.debug("apply plugin watcher")
    ctx.plugin(watcher, argv)
    Logger.debug("apply plugin cmd")
    ctx.plugin(cmd, argv)
}