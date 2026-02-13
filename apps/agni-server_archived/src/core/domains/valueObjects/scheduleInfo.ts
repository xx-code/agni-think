import UnExpectedError from "@core/errors/unExpectedError";
import { mapperPeriod, Period } from "../constants";
import { MomentDateService } from "../entities/libs";
import ValueObject from "./valueObject";

export type SchedulerRecurrence = {
    period: Period 
    interval: number
}

export class Scheduler extends ValueObject {
    
    public dueDate: Date 
    public repeater?: SchedulerRecurrence
    
    constructor(
        dueDate: Date, 
        repeater?: SchedulerRecurrence) {
        super();
        this.dueDate = dueDate
        this.repeater = repeater
    }

    isEqual(object: Scheduler): boolean {
        return MomentDateService.compareDate(this.dueDate, object.dueDate)  == 0 && this.repeater?.interval === object.repeater?.interval && this.repeater?.period === object.repeater?.period
    } 


    toJson(): string {
        return JSON.stringify({
            repeater: this.repeater ? {
                period: this.repeater?.period, 
                interval: this.repeater?.interval
            } : undefined, 
            due_date:this.dueDate, 
        })
    }

    static fromJson(value: any): Scheduler {
        try {
            if (value.due_date === undefined)
                throw new UnExpectedError("Due date undefined")

            const object: {
                repeater?: {period: string, interval: number}, 
                due_date: string
            } = value;

            const schedule = new Scheduler(new Date(object.due_date), object.repeater ? {period: mapperPeriod(object.repeater.period), interval: object.repeater.interval} : undefined)

            return schedule
        } catch(err) {
            throw err
        }
    }
}