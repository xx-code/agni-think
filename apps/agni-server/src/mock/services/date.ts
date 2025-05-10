import { DateService } from "@core/adapters/libs";
import { Period } from "@core/domains/constants";

export class MockDateService implements DateService {
    formatDate(date: string): string {
        return "2025-03-23"; // Mocked formatted date
    }
    
    formatDateWithtime(date: string): string {
        return "2025-03-23 14:30:00"; // Mocked formatted date with time
    }
    
    getTodayWithTime(): string {
        return "2025-03-23 14:30:00"; // Mocked current date with time
    }
    
    getToday(): string {
        return "2025-03-23"; // Mocked current date
    }
    
    getDateAddition(date: string, period: Period, periodTime: number): string {
        return "2025-03-30"; // Mocked date after addition
    }
    
    compareDate(date1: string, date2: string): 0 | 1 | -1 {
        if (date1 === date2) return 0;
        return date1 > date2 ? 1 : -1;
    }
}