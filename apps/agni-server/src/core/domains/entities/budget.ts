import { ValueError } from "../../../core/errors/valueError";
import { Period } from "../constants";
import { isEmpty } from "../helpers";

export class Budget {
    protected id: string
    protected isArchived: boolean
    protected title: string 
    protected target: number
    protected dateStart: string 
    protected dateUpdate: string 
    protected period: Period | null
    protected periodTime: number
    protected dateEnd: string | null

    private change: boolean = false

        // computeCurrentSpend(records: Record[]): Money {
    //     let price_records = records.map(record => record.amount);
    //     let current_spend = price_records.reduce((accumulator, current_value) => accumulator + current_value.getAmount(), 0);
        
    //     let to_money = new Money(current_spend)

    //     return to_money;
    // }

    constructor(id: string, isArchive: boolean, target: number, title: string, dateStart: string, period: Period | null, 
        periodTime: number, dateEnd: string|null, dateUpdate: string) {
        this.id = id
        this.isArchived = isArchive
        this.target = target
        this.title = title
        this.dateStart = dateStart
        this.period = period
        this.periodTime = periodTime
        this.dateUpdate = dateUpdate
        this.dateEnd = dateEnd
    }

    setId(id: string) {
        this.id = id
    }

    getId() {
        return this.id
    }

    setIsArchive(isArchive: boolean) {
        if (this.isArchived !== isArchive)
            this.change = true
        this.isArchived = isArchive
    }

    getIsArchive(): boolean {
        return this.isArchived
    }

    setTitle(title: string) {
        if (this.title !== title)
            this.change = true

        this.title = title
    }

    getTitle(): string {
        return this.title
    }

    setTarget(target: number) {
        if (target <= 0) 
            throw new ValueError("Target must be greater than 0")
        
        if (this.target !== target)
            this.change = true 
        this.target = target
    }

    getTarget(): number {
        return this.target
    }


    setDateStart(dateStart: string) {
        if (this.dateStart !== dateStart)
            this.change = true 
        this.dateStart = dateStart
    }

    getDateStart(): string {
        return this.dateStart
    }

    setDateEnd(dateEnd: string | null) {
        if (this.dateEnd !== dateEnd)
            this.change = true 
        this.dateEnd = dateEnd
    }

    getDateEnd(): string | null{
        return this.dateEnd
    }


    setDateUpdate(dateUpdate: string) {
        if (this.dateUpdate !== dateUpdate)
            this.change = true 
        this.dateUpdate = dateUpdate
    }

    getDateUpdate(): string {
        return this.dateUpdate
    }
    
    setPeriod(period: Period|null) {
        if (this.period !== period)
            this.change = true 
        this.period = period
    }

    getPeriod(): Period | null {
        return this.period
    }

    setPeriodTime(periodTime: number) {
        if (periodTime <= 0)
            throw new ValueError("Period time must be greater than 0")

        if (this.periodTime !== periodTime)
            this.change = true

        this.periodTime = periodTime
    }

    getPeriodTime(): number {
        return this.periodTime
    }

    hasChange(): boolean {
        return this.change
    }
}
