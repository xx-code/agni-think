import { DateService } from "@core/adapters/libs";
import { Period } from "@core/domains/constants";

export class MockDateService implements DateService {
    formatDate(date: string): Date {
        throw new Error("Method not implemented.");
    }

    formatDateWithtime(date: string): Date {
        throw new Error("Method not implemented.");
    }

    getTodayWithTime(): Date {
        throw new Error("Method not implemented.");
    }

    getToday(): Date {
        throw new Error("Method not implemented.");
    }

    getUTCDateAddition(date: Date, period: Period, periodTime: number): Date {
        throw new Error("Method not implemented.");
    }

    isValidDateFormat(dateStr: string): boolean {
        throw new Error("Method not implemented.");
    }

    compareDate(date1: string, date2: string): 0 | 1 | -1 {
        if (date1 === date2) return 0;
        return date1 > date2 ? 1 : -1;
    }
}