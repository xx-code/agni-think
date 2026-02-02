import { TransactionType, TransactionStatus } from '@core/domains/constants';
import Entity, { TrackableProperty } from "./entity";
import { ValueObjectCollection } from '../valueObjects/collection';
import { TransactionDeduction } from '../valueObjects/transactionDeduction';

// Refactoring

export class Transaction extends Entity {
    private accountRef: TrackableProperty<string>
    private type: TrackableProperty<TransactionType>
    private status: TrackableProperty<TransactionStatus>
    private date: TrackableProperty<Date>
    private deductions: ValueObjectCollection<TransactionDeduction>
    private isFreeze: boolean

    constructor(id: string, accountRef: string, date: Date, type: TransactionType, 
        status: TransactionStatus, isFreeze:boolean=false, deductions: TransactionDeduction[] = [],) {
        super(id)
        this.date = new TrackableProperty(date, this.markHasChange.bind(this))
        this.accountRef = new TrackableProperty<string>(accountRef, this.markHasChange.bind(this))
        this.isFreeze = isFreeze;
        this.type = new TrackableProperty<TransactionType>(type, this.markHasChange.bind(this))
        this.status = new TrackableProperty<TransactionStatus>(status, this.markHasChange.bind(this))
        this.deductions = new ValueObjectCollection<TransactionDeduction>(deductions, this.markHasChange.bind(this))
    }

    setAccountRef(accountRef: string) {
        this.accountRef.set(accountRef)
    }

    getAccountRef(): string {
        return this.accountRef.get()
    }

    setDate(date: Date) {
        this.date.set(date)
    }

    getDate(): Date {
        return this.date.get()
    }

    setIsFreeze() {
        this.isFreeze = true
    }

    getIsFreeze(): boolean {
        return this.isFreeze
    }

    setTransactionType(type: TransactionType) {
        this.type.set(type)
    }

    getTransactionType(): TransactionType {
        return this.type.get()
    }

    setStatus(newStatus: TransactionStatus) {
        this.status.set(newStatus)
    }

    getStatus(): TransactionStatus {
        return this.status.get()
    }

    addDeduction(deduction: TransactionDeduction) {
        this.deductions.add(deduction) 
    }

    deleteDeduction(deduction: TransactionDeduction) {
        this.deductions.delete(deduction)
    }

    setDeductions(deductions: TransactionDeduction[]) {
        this.deductions = new ValueObjectCollection<TransactionDeduction>(deductions, this.markHasChange.bind(this))
    }

    getCollectionDeductions(): TransactionDeduction[] {
        return this.deductions.get()
    }
}
