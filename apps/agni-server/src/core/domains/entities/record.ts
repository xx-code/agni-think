import { Money } from "./money"

export enum TransactionType {
    DEBIT = 'Debit',
    CREDIT = 'Credit'
} // TOOD: refactoring by transaction mouvement

export type typeTransactionType = keyof typeof TransactionType 


export class Record {
    private id: string = ''
    private money: Money  
    private date: string
    private description: string = ''
    private transactionType: TransactionType 

    private change: boolean = false

    constructor(id: string, money: Money, date: string, type: TransactionType, description: string = '') {
        this.id = id
        this.money = money
        this.date = date
        this.transactionType = type
        this.date = date 
        this.description = description
    }

    setId(id: string) {
        this.id = id
    }

    getId(): string{
        return this.id
    }

    setMoney(money: Money) {
        if (this.money.getAmount() !== money.getAmount())
            this.change = true 
        
        this.money = money
    }

    getMoney(): Money {
        return this.money
    }

    setDate(date: string) {
        if (this.date !== date)
            this.change = true

        this.date = date 
    }

    getDate(): string {
        return this.date
    }

    setType(type: TransactionType) {
        if (this.transactionType !== type)
            this.change = true 

        this.transactionType = type
    }

    getType(): TransactionType {
        return this.transactionType
    }

    setDescription(description: string) {
        if(this.description !== description)
            this.change = true

        this.description = description
    }

    getDescription(): string {
        return this.description
    }

    hasChange(): boolean {
        return this.change
    }
}