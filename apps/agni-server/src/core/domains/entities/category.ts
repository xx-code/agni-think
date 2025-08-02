import { ValueError } from "@core/errors/valueError"
import { formatted, isValidColor, isEmpty, reverseFormatted, isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"

export class Category extends Entity {
    private title: TrackableProperty<string>
    private iconId: TrackableProperty<string>
    private color: TrackableProperty<string>
    private isSystem: TrackableProperty<boolean>

    constructor(id: string, title: string, iconId: string, color:string='', isSystem=false) {
        super(id)
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.iconId = new TrackableProperty<string>(iconId, this.markHasChange.bind(this))
        this.isSystem = new TrackableProperty<boolean>(isSystem, this.markHasChange.bind(this))

        if (!isEmpty(color) && !isValidColor(color))
            throw new ValueError(`Color: ${color} value not valid`)
        this.color = new TrackableProperty<string>(color, this.markHasChange.bind(this))
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setColor(color:string) {
        if (!isEmpty(color) && !isValidColor(color))
            throw new ValueError(`Color: ${color} value not valid`)
        this.color.set(color)
    }

    setIconId(iconId: string) {
        this.iconId.set(iconId)
    }

    getColor(): string {
        return this.color.get()
    }

    getIconId(): string {
        return this.iconId.get()
    }

    getIsSystem(): boolean {
        return this.isSystem.get()
    }

    setIsSytem(isSystem: boolean) {
        this.isSystem.set(isSystem)
    }
}