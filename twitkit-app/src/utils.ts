export class config {
    host?: {                // 微服务REST域名
        translator: string, // 烤推机
        store: string       // DB
    }
    listen?: number[]       // 允许使用指令的群号
    private?: boolean       // 是否允许私聊上班
    prefix?: string         // 快捷指令前缀
    ispro?: boolean         // 是否发图（考虑ctx.sender.canSendImage）
    cut?: number            // 消息预览截断长度
    port?: number           // 监视器推送端口
    target?: {              // 监视器更新推送目标
        discuss: number[]   // 讨论组
        private: number[]   // 私聊
        group: number[]     // 群
    }
}

/**
 * guid: "{"GUID"}"
 * GUID: hex{8}"-"hex{4}"-"hex{4}"-"hex{4}"-"hex{12}
 * hex: [0-9A-Fa-f]
 */
export type uuid = string
export function verifyUuid(str: uuid): boolean {
    return /[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}/.test(str)
}

const chars = "0123456789abcdef"
function randstr(count: number = 1) {
    let msg: string = ""
    while (msg.length < count) msg += chars[Math.random() * 16 | 0]
}

export function genUuid(): uuid {
    return randstr(8) + '-' + randstr(4) + '-' + randstr(4) + '-' + randstr(4) + '-' + randstr(12)
}

/**
 * guid: "{"GUID"}"
 * GUID: \d+"-"(0\d|1[0-2])"-"([0-2]\d|3[0-1])"T"([0-1]\d|2[0-3])":"[0-5]\d":"[0-5]\d"."\d{3}"+"(0\d|1[0-3])":"[0,3]0
 */
export type ISO8601 = string
export function verifyDatetime(str: string): boolean {
    return /\d+-(0\d|1[0-2])-([0-2]\d|3[0-1])T([0-1]\d|2[0-3]):[0-5]\d(:[0-5]\d(\.\d{1,3})?)?(\+(0\d|1[0-3]):[0,3]0)?/.test(str)
}

export class request {
    forwardFrom: string
    timestamp: ISO8601
    taskId: uuid
    data: any

    constructor (Data: any, Forward?: string, Time?: ISO8601) {
        this.forwardFrom = Forward || "twitkit-app"
        this.timestamp = verifyDatetime(Time) ? Time : new Date().toISOString()
        this.taskId = genUuid()
        this.data = Data
    }

    static parse(Data: request): request {
        if (typeof Data.forwardFrom === "undefined") throw "'forwardFrom' undefined"
        if (!Data.timestamp || !verifyDatetime(Data.timestamp)) throw "'timestamp' not match"
        return Data
    }
}

export class response {
    code: number
    msg: string
    data?: any

    constructor (Msg: string, Code: number = 0, Data: any = undefined) {
        this.msg = Msg
        this.code = Code
        if (typeof Data !== "undefined") this.data = Data
    }

    public toString(this: response): string {
        return JSON.stringify(this)
    }
}

export default {
    config,
    genUuid,
    verifyUuid,
    verifyDatetime,
    request,
    response
}