import { ValueError } from "@core/errors/valueError";
import { mapperPeriod, Period } from "../constants";
import ValueObject from "./valueObject";
import { MomentDateService } from "../entities/libs";

export class Scheduler extends ValueObject { 
    private period: Period
    private periodTime?: number
    private startedDate: Date
    private updatedDate?: Date 
    private endingDate?: Date
    
    constructor(
        period: Period, 
        startedDate: Date, 
        periodTime?: number,
        endingDate?: Date) {
        super();
        this.assertBuildScheduler(periodTime, endingDate) 

        if (periodTime === undefined && endingDate !== undefined)
            this.updatedDate = endingDate
        else if(periodTime !== undefined)
            this.updatedDate = MomentDateService.getDateAddition(startedDate, period, periodTime!) 

        this.period = period
        this.periodTime = periodTime
        this.startedDate = startedDate

        

        this.endingDate = endingDate
    }

    updateSheduler(
        period: Period,  
        startedDate: Date, 
        updatedDate: Date, periodTime?: number, endingDate?: Date) 
    {            
        this.assertBuildScheduler(periodTime, endingDate)

        if (periodTime === undefined && endingDate !== undefined)
            this.updatedDate = endingDate
        else if(periodTime !== undefined) {
            var updatedDate = MomentDateService.getDateAddition(startedDate, period, periodTime!)
            if (MomentDateService.compareDate(updatedDate.toString(), endingDate!.toString()) == 1)
                this.updatedDate = endingDate
            else 
                this.updatedDate =  updatedDate;
        }

        this.period = period
        this.periodTime = periodTime
        this.startedDate = startedDate
        this.updatedDate = updatedDate
        this.endingDate = endingDate
    }

    getPeriod(): Period {
        return this.period
    }

    getPeriodTime(): number | undefined {
        return this.periodTime
    } 

    getStartedDate(): Date {
        return this.startedDate
    }
    
    getUpdatedDate(): Date {
        return this.updatedDate!
    }

    getEndingDate(): Date | undefined {
        return this.endingDate
    }

    private assertBuildScheduler(
        periodTime?: number, 
        endingDate?: Date
    ) {
        if (periodTime === undefined && endingDate === undefined)
            throw new ValueError("SCHEDULER_WITH_PERIOD_UNDETERMINED_HAVE_NOT_END_DATE")
    }

    isDue(): boolean {
        if (!this.updatedDate)
            return false;

        return MomentDateService.compareDateWithDate(new Date(), this.updatedDate) >= 0
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
            startedDate:this.startedDate, endingDate: this.endingDate})
    }

   static fromJson(value: any): Scheduler {
        try {
            const object: {
                period: string, 
                periodTime?: number,
                startedDate: string,
                endingDate?: string
            } = value;

            var endDate  = object.endingDate ? new Date(object.endingDate) : undefined

            return new Scheduler(mapperPeriod(object.period), new Date(object.startedDate) , object.periodTime, endDate)
        } catch(err) {
            throw err
        }
        
    }
}