import { TransactionStatus, TransactionType } from "../constants";
import Entity, { TrackableProperty } from "./entity";

export class PatrimonySnapshot extends Entity {
    private patrimonyId: TrackableProperty<string>
    private date: TrackableProperty<Date>    
    private currentBalanceObserver: TrackableProperty<number>
    private status: TrackableProperty<TransactionStatus>

    constructor(id: string, patrimonyId: string, currentBalanceObserver: number, status: TransactionStatus, date: Date) {
        super(id)
        this.patrimonyId = new TrackableProperty(patrimonyId, this.markHasChange.bind(this))
        this.currentBalanceObserver = new TrackableProperty(currentBalanceObserver, this.markHasChange.bind(this))
        this.date = new TrackableProperty(date, this.markHasChange.bind(this))
        this.status = new TrackableProperty(status, this.markHasChange.bind(this))
    }

    setPatrimonyId(patrimonyId: string) {
        this.patrimonyId.set(patrimonyId)
    }

    getPatrimonyId(): string {
        return this.patrimonyId.get()
    }

    setCurrentBalanceObserver(currentBalanceObserver: number) {
        this.currentBalanceObserver.set(currentBalanceObserver)
    }

    getCurrentBalanceObserver(): number {
        return this.currentBalanceObserver.get()
    }

    setDate(date: Date) {
        this.date.set(date)
    }

    getDate(): Date {
        return this.date.get()
    }

    getStatus(): TransactionStatus {
        return this.status.get()
    }

    setStatus(type: TransactionStatus) {
        this.status.set(type)
    }
}