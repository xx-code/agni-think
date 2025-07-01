import { ValueError } from "@core/errors/valueError";
import { mapperPeriod, Period } from "../constants";
import ValueObject from "./valueObject";

export class Scheduler extends ValueObject { 
    private period: Period
    private periodTime: number
    private startedDate: Date
    private updatedDate: Date 
    private endingDate: Date | null
    
    constructor(
        period: Period, 
        periodTime: number, 
        startedDate: Date, 
        updatedDate: Date, endingDate: Date|null) {
        super();
        this.assertBuildScheduler(period, periodTime, endingDate) 

        this.period = period
        this.periodTime = periodTime
        this.startedDate = startedDate
        this.updatedDate = updatedDate
        this.endingDate = endingDate
    }

    updateSheduler(
        period: Period, 
        periodTime: number, 
        startedDate: Date, 
        updatedDate: Date, endingDate: Date|null) 
    {            
        this.assertBuildScheduler(period, periodTime, endingDate)

        this.period = period
        this.periodTime = periodTime
        this.startedDate = startedDate
        this.updatedDate = updatedDate
        this.endingDate = endingDate
    }

    getPeriod(): Period {
        return this.period
    }

    getPeriodTime(): number {
        return this.periodTime
    } 

    getStartedDate(): Date {
        return this.startedDate
    }
    
    getUpdatedDate(): Date {
        return this.updatedDate
    }

    getEndingDate(): Date|null {
        return this.endingDate
    }

    private assertBuildScheduler(
        period: Period, 
        periodTime: number, 
        endingDate: Date|null
    ) {
        if (period == Period.UNDETERMINED && periodTime > 0)
            throw new ValueError("SCHEDULER_WITH_PERIOD_UNDETERMINED_HAVE_NOT_PERIOD_TIME")

        if (period == Period.UNDETERMINED && endingDate !== null)
            throw new ValueError("SCHEDULER_WITH_PERIOD_UNDETERMINED_HAVE_NOT_END_DATE")

        if (period !== Period.UNDETERMINED && periodTime <= 0)
            throw new ValueError("SCHEDULER_WITH_PERIOD_DETERMINED_MUST_HAVE_PERIOD_TIME")

        if (period !== Period.UNDETERMINED && endingDate === null)
            throw new ValueError("SCHEDULER_WITH_PERIOD_DETERMINED_MUST_HAVE_AN_ENDING_DATE")
    }

    isEqual(object: Scheduler): boolean {
        if (this.period !== object.getPeriod())
            return false 

        if (this.periodTime !== object.getPeriodTime())
            return false

        if (this.startedDate !== object.getStartedDate())
            return false

        if (this.updatedDate !== object.getUpdatedDate())
            return false

        if (this.endingDate !== object.getEndingDate())
            return false

        return true
    }

    toJson(): string {
        return JSON.stringify({period: this.period, periodTime: this.periodTime, 
            startedDate:this.startedDate, updatedDate: this.updatedDate, endingDate: this.endingDate})
    }

    fromJson(string: string): Scheduler {
        const object: {
            period: string, periodTime: number,
            startedDate: Date, updatedDate: Date,
            endingDate: Date|null
        } = JSON.parse(string);

        return new Scheduler(mapperPeriod(object.period), object.periodTime, object.startedDate, 
            object.updatedDate, object.endingDate)
    }
}