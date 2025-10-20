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
        endingDate?: Date,
        updateDate?: Date
    ) {
        super();
        this.assertBuildScheduler(periodTime, endingDate) 

        // First creation scheduler
        if (updateDate == undefined) {
            if (periodTime === undefined && endingDate !== undefined)
                this.updatedDate = endingDate
            else if(periodTime !== undefined)
                this.updatedDate = MomentDateService.getUTCDateAddition(startedDate, period, periodTime!)
        } else {
            this.updatedDate = updateDate
        }

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
            var updatedDate = MomentDateService.getUTCDateAddition(startedDate, period, periodTime!)
            if (MomentDateService.compareDate(updatedDate, endingDate!) == 1)
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
        return this.updatedDate === undefined ? this.endingDate! : this.updatedDate!
    }

    computeUpdateDate(): Date {
        if(this.periodTime !== undefined) {
            var updatedDate = MomentDateService.getUTCDateAddition(this.startedDate, this.period, this.periodTime)
            return updatedDate
        }

        return this.endingDate!
    }

    updateSchedule() {
        if(this.periodTime !== undefined) {
            var updatedDate = MomentDateService.getUTCDateAddition(this.startedDate, this.period, this.periodTime)
            this.updatedDate = updatedDate
        }
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
        if (this.updatedDate === undefined && this.updatedDate === undefined)
            return false;

        return MomentDateService.compareDateWithDate(new Date(), this.updatedDate == undefined ? this.endingDate! : this.updatedDate ) >= 0
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
            startedDate:this.startedDate, endingDate: this.endingDate, updateDate: this.updatedDate})
    }

   static fromJson(value: any): Scheduler {
        try {
            const object: {
                period: string, 
                periodTime?: number,
                startedDate: string,
                updateDate?: string,
                endingDate?: string
            } = value;

            var endDate  = object.endingDate ? new Date(object.endingDate) : undefined
            var updateDate  = object.updateDate ? new Date(object.updateDate) : undefined

            return new Scheduler(mapperPeriod(object.period), new Date(object.startedDate) , object.periodTime, endDate, updateDate)
        } catch(err) {
            throw err
        }
    }
}