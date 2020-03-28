export const name = 'translator-bot'
import { Context } from 'koishi-core'
import watcher from './watcher'
import cmd from './cmd'
import { config } from './utils'

export {
    watcher
}

export function apply (ctx: Context, argv?: config) {
    // if (argv.ispro && ctx.app.version.coolqEdition !== "pro") argv.ispro = false
    argv.prefix = argv.prefix || "#"
    ctx.plugin(watcher, argv)
    ctx.plugin(cmd, argv)
}