import { ValueError } from "@core/errors/valueError"
import { formatted, isValidColor, isEmpty, reverseFormatted } from "../helpers"

export class Tag {
    private id: string = ''
    private value: string = ''
    private color: string = ''
    private isSystem: boolean = false

    private change: boolean = false 

    constructor(id: string, value: string, color: string ='', isSystem: boolean=false) {
        this.id = id
        this.value = value
        this.color = color
        this.isSystem = isSystem
    }

    setId(id: string) {
        this.id = id 
    }

    getId(): string {
        return this.id 
    }

    setColor(color: string) {
        if (!isEmpty(color) && !isValidColor(color))
            throw new ValueError(`Color: ${color} value not valid`)
        
        if (this.color !== color) 
            this.change = true
        
        this.color = color
    }

    getColor(): string {
        return this.color
    }

    setValue(value: string) {
        if (this.value !== value)
            this.change = true

        this.value = formatted(value)
    }

    getValue(): string {
        return reverseFormatted(this.value)
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