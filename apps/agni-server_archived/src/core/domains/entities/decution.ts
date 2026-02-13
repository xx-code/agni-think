import { ValueError } from "@core/errors/valueError"
import { DeductionBase, DeductionMode } from "../constants"
import Entity, { TrackableProperty } from "./entity"
import { isStringDifferent } from "../helpers"

export class DeductionType extends Entity {
    private title: TrackableProperty<string>
    private base:  DeductionBase
    private mode: DeductionMode
    private description: TrackableProperty<string>

    constructor(id: string, title: string, description: string, base: DeductionBase, mode: DeductionMode,) {
        super(id)
        this.base = base
        this.mode = mode
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))  
        this.description = new TrackableProperty<string>(description, this.markHasChange.bind(this))
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setDescription(description: string) { 
        this.title.set(description, isStringDifferent)
    }

    getDescription(): string {
        return this.description.get()
    }

    getBase(): string {
        return this.base
    }

    getMode(): string {
        return this.mode
    }
}

export class Deduction extends Entity { 
    private typeId: string 
    private rate: TrackableProperty<number> 

    constructor(id: string, typeId: string,  rate: number) {
        super(id)
        this.typeId = typeId

        if (rate < 0)
            throw new ValueError("Rate must be great 0")

        this.rate = new TrackableProperty<number>(rate, this.markHasChange.bind(this))
    }

    getDeductionTypeId(): string {
        return this.typeId
    }

    setRate(rate: number) {
        if (rate < 0)
            throw new ValueError("Rate must be great than 0")

        this.rate.set(rate)
    }

    getRate(): number {
        return this.rate.get()
    } 
}
