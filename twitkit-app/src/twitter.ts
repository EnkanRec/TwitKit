export class twitter {
    url:    string
    content:string
    media:  string[]
    published: boolean
    comment?: string
    trans?: string
    img?:   string
    user?: {
        name: string
        description: string
        image: string
        link: string
    }
}