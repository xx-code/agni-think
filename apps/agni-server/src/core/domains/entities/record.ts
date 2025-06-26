import { isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"
import { Money } from "./money"

export enum TransactionType {
    DEBIT = 'Debit',
    CREDIT = 'Credit'
}

export type typeTransactionType = keyof typeof TransactionType 


export class Record extends Entity {
    private money: TrackableProperty<Money>   
    private date: TrackableProperty<string>
    private description: TrackableProperty<string>
    private transactionType: TrackableProperty<TransactionType> 

    constructor(id: string, money: Money, date: string, type: TransactionType, description: string = '') {
        super(id)
        this.money = new TrackableProperty(money, this.markHasChange)
        this.date = new TrackableProperty(date, this.markHasChange)
        this.transactionType = new TrackableProperty(type, this.markHasChange)
        this.date = new TrackableProperty(date, this.markHasChange) 
        this.description = new TrackableProperty(description, this.markHasChange)
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

    getDate(): string {
        return this.date.get()
    }

    setType(type: TransactionType) {
        this.transactionType.set(type)
    }

    getType(): TransactionType {
        return this.transactionType.get()
    }

    setDescription(description: string) {
        this.description.set(description, isStringDifferent)
    }

    getDescription(): string {
        return this.description.get()
    }
}