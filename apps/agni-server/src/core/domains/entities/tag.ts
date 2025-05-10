import { ValueError } from "@core/errors/valueError"
import { formatted, isValidColor, isEmpty, reverseFormatted } from "../helpers"

export class Tag {
    private id: string = ''
    private value: string = ''
    private color: string = ''
    private change: boolean = false 

    constructor(id: string, value: string, color: string ='') {
        this.id = id
        this.value = value
        this.color = color
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

    hasChange(): boolean {
        return this.change
    }
}