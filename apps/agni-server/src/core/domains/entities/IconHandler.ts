import { isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"

class IconHandler extends Entity {
    private name: TrackableProperty<string>
    private heberger: TrackableProperty<string>

    constructor(id: string, name: string, heberger: string) {
        super(id)
        this.name = new TrackableProperty<string>(name, this.markHasChange) 
        this.heberger = new TrackableProperty<string>(heberger, this.markHasChange)
    }

    setName(name: string) {
        this.name.set(name, isStringDifferent)
    }

    getName(): string {
        return this.name.get()
    }

    setHeberger(heberger: string) {
        this.heberger.set(heberger, isStringDifferent)
    }

    getHeberger(): string {
        return this.heberger.get()
    }
}