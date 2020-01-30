export const name = 'translator-bot'
import { Context } from 'koishi-core'
import watcher from './watcher'
import cmd from './cmd'

export {
    watcher
}

export function apply (ctx: Context, argv = {}) {
    ctx.plugin(watcher, argv)
    ctx.plugin(cmd, argv)
}