import { HoldingTransactionType } from "../constants";
import Entity, { TrackableProperty } from "./entity";
import { Money } from "./money";

export class HoldingTransaction extends Entity {
    private holdingId: string
    private cost: TrackableProperty<Money>
    private quantity: TrackableProperty<number>
    private fees: TrackableProperty<number>
    private date: TrackableProperty<Date>
    private type: TrackableProperty<HoldingTransactionType>


    constructor(id: string, holdingId: string, cost: Money, quantity: number, fees: number, date: Date, type: HoldingTransactionType) {
        super(id)
        this.holdingId = holdingId
        this.type = new TrackableProperty<HoldingTransactionType>(type, this.markHasChange.bind(this))
        this.cost = new TrackableProperty<Money>(cost, this.markHasChange.bind(this))
        this.quantity = new TrackableProperty<number>(quantity, this.markHasChange.bind(this))
        this.fees = new TrackableProperty<number>(fees, this.markHasChange.bind(this))
        this.date = new TrackableProperty<Date>(date, this.markHasChange.bind(this))
    }

    getHoldingId(): string {
        return this.holdingId
    }

    setCost(cost: Money) {
        this.cost.set(cost)
    }

    getCost(): Money {
        return this.cost.get()
    }

    setQuantity(quantity: number) {
        this.quantity.set(quantity)
    }

    getQuantity(): number {
        return this.quantity.get()
    }

    setFees(fees: number) {
        this.fees.set(fees)
    }

    getFees(): number {
        return this.fees.get()
    }

    setDate(date: Date) {
        this.date.set(date)
    }

    getDate(): Date {
        return this.date.get()
    }

    getType(): HoldingTransactionType {
        return this.type.get()
    }

    setType(type: HoldingTransactionType) {
        this.type.set(type)
    }
}