import { DateService } from "@core/adapters/libs";
import { Period } from "@core/domains/constants";
import { ValueError } from "@core/errors/valueError";
const moment = require('moment');

export class MomentDateService implements DateService {
    constructor() {
        
    }

    formatDate(date: string): string {
        let formatted = moment(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = moment(date).format("YYYY-MM-DD")
        return formatted
    }

    formatDateWithtime(date: string): string {
        let formatted = moment()
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        formatted = formatted.format("YYYY-MM-DDTHH:mm")

        return formatted
    }

    getTodayWithTime(): string {
        let today = moment().format("YYYY-MM-DDTHH:mm")
        return today
    }

    getToday(): string {
        let today = moment().format("YYYY-MM-DD")
        return today
    }

    getDateAddition(date: string, period: Period, periodTime: number): string {
        let formatted = moment(date)
        if (!formatted.isValid())
            throw new ValueError(`${date} is not valid`)

        let value = ""
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

        let dateFormat = formatted.add(periodTime, value).format('YYYY-MM-DD')

        return dateFormat
    }

    compareDate(date1: string, date2: string): 0 | 1 | -1 {
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