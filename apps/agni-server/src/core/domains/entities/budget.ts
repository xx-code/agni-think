import { ValueError } from "../../../core/errors/valueError";
import { Period } from "../constants";
import { isStringDifferent } from "../helpers";
import Entity, { TrackableProperty } from "./entity";

export class Budget extends Entity { 
    private title: TrackableProperty<string> 
    private target: TrackableProperty<number>
    private dateStart: TrackableProperty<string> 
    private dateUpdate: TrackableProperty<string> 
    private period: TrackableProperty<Period | null>
    private periodTime: TrackableProperty<number>
    private dateEnd: TrackableProperty<string | null>
    private isArchived: TrackableProperty<boolean>

    constructor(id: string, isArchive: boolean, target: number, title: string, dateStart: string, period: Period | null, 
        periodTime: number, dateEnd: string|null, dateUpdate: string) {
        super(id)
        this.target = new TrackableProperty<number>(target, this.markHasChange) 
        this.title = new TrackableProperty<string>(title, this.markHasChange)
        this.dateStart = new TrackableProperty<string>(dateStart, this.markHasChange)
        this.period = new TrackableProperty<Period|null>(period, this.markHasChange)
        this.periodTime = new TrackableProperty<number>(periodTime, this.markHasChange)
        this.dateUpdate = new TrackableProperty<string>(dateUpdate, this.markHasChange)
        this.dateEnd = new TrackableProperty<string|null>(dateEnd, this.markHasChange)
        this.isArchived = new TrackableProperty<boolean>(isArchive, this.markHasChange)
    }

    setIsArchive(isArchive: boolean) {
        this.isArchived.set(isArchive)
    }

    getIsArchive(): boolean {
        return this.isArchived.get()
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setTarget(target: number) {
        if (target <= 0) 
            throw new ValueError("Target must be greater than 0")
        this.target.set(target)
    }

    getTarget(): number {
        return this.target.get()
    }


    setDateStart(dateStart: string) {
        this.dateStart.set(dateStart)
    }

    getDateStart(): string {
        return this.dateStart.get()
    }

    setDateEnd(dateEnd: string | null) {
        this.dateEnd.set(dateEnd)
    }

    getDateEnd(): string | null{
        return this.dateEnd.get()
    }


    setDateUpdate(dateUpdate: string) {
        this.dateUpdate.set(dateUpdate)
    }

    getDateUpdate(): string {
        return this.dateUpdate.get()
    }
    
    setPeriod(period: Period|null) {
        this.period.set(period)
    }

    getPeriod(): Period | null {
        return this.period.get()
    }

    setPeriodTime(periodTime: number) {
        if (periodTime <= 0)
            throw new ValueError("Period time must be greater than 0")

        this.periodTime.set(periodTime)
    }

    getPeriodTime(): number {
        return this.periodTime.get()
    }

}
