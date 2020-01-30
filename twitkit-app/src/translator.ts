import { Twitter } from './twitter'
import axios from 'axios'

const host:string = "http://localhost"

async function get(tw: Twitter): Promise<string> {
    let res = await axios.post(host + "/api/cooker/cook", {
        taskId: "",
        tid: tw.id,
        origText: tw.content,
        transText: tw.trans,
        media: tw.media,
        tags: tw.tag,
        avatar: tw.user ? tw.user.image : "",
        displayName: tw.user ? tw.user.name : "",
        postDate: tw.postDate
    })
    return res.data.resultUrl
}

async function getByUrl(url: string, trans: string): Promise<string> {
    return "base64://iVBORw0KGgoAAAANSUhEUgAAAFsAAAAMCAIAAACC31aBAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAIHSURBVEhLrVXbkQIxDKMuCko9W802QzFgOX7HCdzM6eeytiwpXuAe74p7PMYtZ8frej6+opnr1Rj3eF4vOU/8Rj6wHJS3qk/gImHcxPiCGMFGskV62goz74dkh/j/thEkCWDiJriWTc5mjS2fEWpoyQ2xtk4WoF4THhPfIYOWqp0y0jz1GwlHvaugPE7EdyDnoCDwb43SPUPlBtxkuDo26FWoaggyDdnTGE9LWKQP2wqitgGttUEyyZIffv0dSbjHuOO+Dzio+VvqoV2ed9YYKGEdSbh8KKIvndusHCDb0wxtJNdWFDWyhhfLLbkIJ7kktdlpvpreLJDZYBk9b2SeS7SafGJupO8xSu6gTnUCu9R8JR6A5eWa5KOvH/9dwD7qlzfS5M2W6Sln5hIzUIxidKZ+2Aj9zZBqUrGHUle6QgwVYPjz5DcvGogmMVxwzlYT/RjDHsoNUZQed+Z83oirhKoYYSj1QwCA+qniS8BkIQOukJz/tBE/65YR0q0ZRoqZiYMi9eaMlOtGMqTKKm4tWDbSYfULSAquv98I/YPDaq8QhTXERafwGLP5foSoQNF9Cfyw/Ywodjf/shE1ny+MsLI3CvLyBKoDCD3kzNsD2C1fg/g26WyxiZeeattfVpZmNE1Ce5/DkLeoJ/fMJGOYMNNWG5lmJAk0lB78jCSZZZ7PkYbS6/0BwhLl3Ql1BCMAAAAASUVORK5CYII="
}

export default {
    get,
    getByUrl
}