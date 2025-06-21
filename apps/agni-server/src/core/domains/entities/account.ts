import { AccountType } from "../constants"
import { isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"
import { Money } from "./money"

export class Account extends Entity {
    private title: TrackableProperty<string>
    private balance: TrackableProperty<number>
    private type: TrackableProperty<AccountType>

    constructor(id: string, title: string, type: AccountType, balance: number = 0) {
        super(id)
        this.title = new TrackableProperty<string>(title, this.markHasChange)
        this.type = new TrackableProperty<AccountType>(type, this.markHasChange)
        this.balance = new TrackableProperty<number>(balance, this.markHasChange)
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setType(type: AccountType) {
        this.type.set(type)
    }

    getType(): AccountType {
        return this.type.get()
    }

    setBalance(balance: number) {
        this.setBalance(balance)
    }

    getBalance(): number {
        return this.balance.get()
    }

    addOnBalance(money: Money) {
        const newBalance = this.getBalance() + money.getAmount()
        this.setBalance(newBalance)
    }

    substractBalance(money: Money) {
        const newBalance = this.getBalance() - money.getAmount()
        this.setBalance(newBalance)
    }
}
