import { DateService } from "@core/adapters/libs";
import { ValueError } from "@core/errors/valueError";
import { Period } from "../constants";

export function searchInArray(value: string, array: string[]) : string[] {
    let results = array.filter(value_arr => value_arr.toLowerCase().search(value.toLowerCase()) !== -1);
    return results;
}

const moment = require('moment');
moment.defineLocale('fr', [])

function isValidDateFormat(dateStr: string) {
    // bot code 
    const iso8601Regex = /^\d{4}-\d{2}-\d{2}(T\d{2}:\d{2}(:\d{2})?(Z|([+-]\d{2}:\d{2}))?)?$/;
    const rfc2822Regex = /^[a-zA-Z]{3}, \d{2} [a-zA-Z]{3} \d{4} \d{2}:\d{2}:\d{2} GMT$/;

    return iso8601Regex.test(dateStr) || rfc2822Regex.test(dateStr);
}


// Brock inner deps
export class MomentDateService {

    static periodMatcherToMoment(period: Period): string {
        let value = ""
        switch(period) {
            case Period.DAY: 
                value = "days"
                break;
            case Period.MONTH:
                value = "months"
                break;
            case Period.WEEK:
                value = "weeks"
                break;
            case Period.YEAR:
                value = "years"
                break;
            default:
                value = "days"
                break;
        }
        return value
    }

    static assertValidDate(dateStr: string) {
        if (!isValidDateFormat(dateStr))
            throw new ValueError(`Date ${dateStr} format is not valid`)
    }

    static formatDate(date: string):Date {
        // this.assertValidDate(date)

        let formatted = moment.utc(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = moment.utc(date).format("YYYY-MM-DD")
        return new Date(formatted)
    }
    static formatDateStr(date: string):string {
        // this.assertValidDate(date)

        let formatted = moment.utc(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = moment.utc(date).format("YYYY-MM-DD")
        return formatted
    }

    static formatDateWithtime(date: string): Date {
        // this.assertValidDate(date)

        let formatted = moment(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = formatted.format("YYYY-MM-DDTHH:mm")

        return new Date(formatted)
    }

    static getTodayWithTime(): Date {
        let today = moment.utc().format("YYYY-MM-DDTHH:mm")
        return new Date(today)
    }

    static getToday(): Date {
        let today = moment.utc().format("YYYY-MM-DD")
        return new Date(today)
    }

    static getUTCDateAddition(date: Date, period: Period, periodTime: number, strictDatebefore: boolean=true): Date {
        let formatted = moment.utc(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)
      
        let today = moment()    
        const periodStr = this.periodMatcherToMoment(period) 

        let dateFormat = formatted.add(periodTime, periodStr)

        if (dateFormat.isBefore(today)) {
            var diff = today.diff(dateFormat, periodStr)
            if (diff === 0 && !strictDatebefore) diff += 1
            dateFormat = dateFormat.add(periodTime * diff, periodStr)
        }

        return new Date(dateFormat) 
    }

    static getUTCDateSubstraction(date: Date, period: Period, periodTime: number): Date {
        let formatted = moment.utc(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)
      
        let today = moment.utc()    
        const value = this.periodMatcherToMoment(period) 

        let dateFormat = formatted.subtract(periodTime, value)

        if (dateFormat.isAfter(today)) {
            var diff = dateFormat.diff(today, value)
            dateFormat = dateFormat.subtract(periodTime * diff, value)
        }

        return new Date(dateFormat) 
    }

    static compareDate(date1: string, date2: string): 0 | 1 | -1 {
        // MomentDateService.assertValidDate(date1)
        // MomentDateService.assertValidDate(date2)

        let formatted1 = moment.utc(date1)
        if (!formatted1.isValid())
            throw new ValueError(`date1: ${date1} is not valid`)

        let formatted2 = moment(date2)
        if (!formatted2.isValid())
            throw new ValueError(`date2: ${date2} is not valid`)
        
        if (formatted1.isBefore(formatted2))
            return -1

        if (formatted1.isAfter(formatted2))
            return 1

        return 0
    }

    static compareDateWithDate(date1: Date, date2: Date): 0 | 1 | -1{
        let formatted1 = moment.utc(date1)
        if (!formatted1.isValid())
            throw new ValueError(`date1: ${date1} is not valid`)

        let formatted2 = moment.utc(date2)
        if (!formatted2.isValid())
            throw new ValueError(`date2: ${date2} is not valid`)
        
        if (formatted1.isBefore(formatted2))
            return -1

        if (formatted1.isAfter(formatted2))
            return 1

        return 0
    }

    static getUTCDateByPeriod(period: Period, periodTime: number): {startDate: Date, endDate: Date} {
        const momentPeriod = this.periodMatcherToMoment(period); 
        const startDate = moment.utc().startOf(momentPeriod);
        const endDate = moment.utc().add(periodTime, momentPeriod).startOf(momentPeriod);

        return { 
            startDate: new Date(startDate.format("YYYY-MM-DD")),
            endDate: new Date(endDate.format("YYYY-MM-DD"))
         }
    }
}

