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

    static assertValidDate(dateStr: string) {
        if (!isValidDateFormat(dateStr))
            throw new ValueError(`Date ${dateStr} format is not valid`)
    }

    static formatDate(date: string): Date {
        this.assertValidDate(date)

        let formatted = moment(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = moment(date).format("YYYY-MM-DD")
        return formatted
    }

    static formatDateWithtime(date: string): Date {
        this.assertValidDate(date)

        let formatted = moment(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = formatted.format("YYYY-MM-DDTHH:mm")

        return formatted
    }

    static getTodayWithTime(): Date {
        let today = moment().format("YYYY-MM-DDTHH:mm")
        return new Date(today)
    }

    static getToday(): Date {
        let today = moment().format("YYYY-MM-DD")
        return new Date(today)
    }

    static getDateAddition(date: Date, period: Period, periodTime: number): Date {
        let formatted = moment(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)
      
        let value = ""
        let today = moment()
        switch(period) {
            case Period.DAY: 
                value = "days"
                break
            case Period.MONTH:
                value = "months"
                break
            case Period.WEEK:
                value = "weeks"
                break
            case Period.YEAR:
                value = "years"
                break
        }

        let dateFormat = formatted.add(periodTime, value)

        if (dateFormat.isBefore(today)) {
            var diff = dateFormat.diff(today, value)
            dateFormat = dateFormat.add(periodTime * diff, value)
        }

        return new Date(dateFormat) 
    }

    compareDate(date1: string, date2: string): 0 | 1 | -1 {
        MomentDateService.assertValidDate(date1)
        MomentDateService.assertValidDate(date2)

        let formatted1 = moment(date1)
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
}

