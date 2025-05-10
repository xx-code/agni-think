import { AccountType } from "../constants"
import { Money } from "./money"

export class Account {
    private id: string = ''
    private title: string = ''
    private isSaving: boolean = false
    private balance: number = 0
    private type: AccountType

    private change: boolean = false

    constructor(id: string, title: string, type: AccountType, balance: number = 0) {
        this.id = id
        this.title = title
        this.type = type
        this.balance = balance
    }

    setId(id: string) {
        this.id = id
    }

    getId() {
        return this.id
    }

    setTitle(title: string) {
        if (this.title !== title)
            this.change = true 

        this.title = title
    }

    getTitle(): string {
        return this.title
    }

    setIsSaving(isSaving: boolean) {
        this.isSaving = isSaving
    }

    setType(type: AccountType) {
        if (type !== this.type) 
            this.change = true
        this.type = type
    }

    getType(): AccountType {
        return this.type
    }

    setBalance(balance: number) {
        if (balance !== this.balance) 
            this.change = true

        this.balance = balance
    }

    getBalance(): number {
        return this.balance
    }

    addOnBalance(money: Money) {
        this.change = true
        this.balance += money.getAmount()
    }

    substractBalance(money: Money) {
        this.change = true
        this.balance -= money.getAmount()
    }
 
    getIsSaving(): boolean {
        return this.isSaving
    }

    hasChange(): boolean {
        return this.change
    }
}
