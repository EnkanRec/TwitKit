export class Twitter {
    id:     number
    url:    string
    content:string
    media:  string[]
    published: boolean
    type: string
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