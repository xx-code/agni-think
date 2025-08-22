import { ValueError } from "@core/errors/valueError"
import { isValidColor, isEmpty, isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"

export class Tag extends Entity {
    private value: TrackableProperty<string>
    private color: TrackableProperty<string>
    private isSystem: TrackableProperty<boolean>

    constructor(id: string, value: string, color: string ='', isSystem: boolean=false) {
        super(id)
        this.value = new TrackableProperty<string>(value, this.markHasChange.bind(this))

        if (!isEmpty(color) && !isValidColor(color))
            throw new ValueError(`Color: ${color} value not valid`)
        this.color = new TrackableProperty<string>(color, this.markHasChange.bind(this))

        this.isSystem = new TrackableProperty<boolean>(isSystem, this.markHasChange.bind(this))
    }

    setColor(color: string) {
        if (!isEmpty(color) && !isValidColor(color))
            throw new ValueError(`Color: ${color} value not valid`)
        this.color.set(color)
    }

    getColor(): string {
        return this.color.get()
    }

    setValue(value: string) {
        this.value.set(value, isStringDifferent)
    }

    getValue(): string {
        return this.value.get()
    }

    getIsSystem(): boolean {
        return this.isSystem.get()
    }

    setIsSytem(isSystem: boolean) {
        this.setIsSytem(isSystem)
    }
}