import { RecordType } from "../constants"
import { isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"
import { Money } from "./money"


export class Record extends Entity {
    private money: TrackableProperty<Money>   
    private date: TrackableProperty<string>
    private description: TrackableProperty<string>
    private type: TrackableProperty<RecordType> 

    constructor(id: string, money: Money, date: string, type: RecordType, description: string = '') {
        super(id)
        this.money = new TrackableProperty(money, this.markHasChange.bind(this))
        this.date = new TrackableProperty(date, this.markHasChange.bind(this))
        this.type = new TrackableProperty(type, this.markHasChange.bind(this))
        this.date = new TrackableProperty(date, this.markHasChange.bind(this)) 
        this.description = new TrackableProperty(description, this.markHasChange.bind(this))
    }

    setMoney(money: Money) {
        this.money.set(money)
    }

    getMoney(): Money {
        return this.money.get()
    }

    setDate(date: string) {
        this.date.set(date)
    }

    getUTCDate(): string {
        return this.date.get()
    }

    setType(type: RecordType) {
        this.type.set(type)
    }

    getType(): RecordType {
        return this.type.get()
    }

    setDescription(description: string) {
        this.description.set(description, isStringDifferent)
    }

    getDescription(): string {
        return this.description.get()
    }
}