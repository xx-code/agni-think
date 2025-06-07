import { ValueError } from "@core/errors/valueError"
import { formatted, isValidColor, isEmpty, reverseFormatted } from "../helpers"

export class Category {
    private id: string = ''
    private title: string = ''
    private iconId = ''
    private color: string = ''
    private isSystem: boolean = false

    private change: boolean = false

    constructor(id: string, title: string, iconId: string, color:string='', isSystem=false) {
        this.id = id
        this.setTitle(title)
        this.iconId = iconId
        this.setColor(color)
        this.isSystem = isSystem
    }

    setId(id: string) {
        this.id = id 
    }

    getId(): string {
        return this.id
    }

    setTitle(title: string) {
        if (this.title !== title)
            this.change = true

        this.title = formatted(title)
    }

    getTitle(): string {
        return reverseFormatted(this.title)
    }

    setColor(color:string) {
        if (!isEmpty(color) && !isValidColor(color))
            throw new ValueError(`Color: ${color} value not valid`)

        if (this.color !== color) 
            this.change = true

        this.color = color
    }

    setIconId(iconId: string) {
        if (this.iconId !== iconId)
            this.change = true

        this.iconId = iconId
    }

    getColor(): string {
        return this.color
    }

    getIconId(): string {
        return this.iconId
    }

    getIsSystem(): boolean {
        return this.isSystem
    }

    setIsSytem(isSystem: boolean) {
        if (this.isSystem !== isSystem)
            this.change = true

        return this.isSystem
    }

    hasChange(): boolean {
        return this.change
    }
}