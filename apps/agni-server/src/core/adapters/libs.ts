import { Period } from "../domains/constants";

import { v4 as uuidv4 } from 'uuid';
import { Money } from "../domains/entities/money";

export const GetUID = () => {
    return uuidv4().toString()
}

export interface DateService {
    formatDate(date: string): string
    formatDateWithtime(date: string): string
    getTodayWithTime(): string 
    getToday(): string
    getDateAddition(date: string, period: Period, periodTime: number): string
    compareDate(date1: string, date2: string): 0|1|-1
}

export type ScrappingPriceRequest = {
    link: string
    html: string
}

export interface ScrappingPriceService {
    getPrice(request: ScrappingPriceRequest): Promise<Money>
}
