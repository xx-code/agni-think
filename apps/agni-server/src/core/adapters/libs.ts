import { Period } from "../domains/constants";

import { v4 as uuidv4 } from 'uuid';
import { Money } from "../domains/entities/money";

export const GetUID = () => {
    return uuidv4().toString()
}

export interface DateService {
    formatDate(date: string): Date
    formatDateWithtime(date: string): Date
    getTodayWithTime(): Date 
    getToday(): Date
    getDateAddition(date: Date, period: Period, periodTime: number): Date
    compareDate(date1: string, date2: string): 0|1|-1
}

export type ScrappingPriceRequest = {
    link: string
    html: string
}

export interface ScrappingPriceService {
    getPrice(request: ScrappingPriceRequest): Promise<Money>
}

export type TaskTimer = {
    seconde?: number
    minute?: number
    hour?: number
    dayOfMonth?: number
    month?: number
    dayOfweek?: number
}

export interface TaskScheduler {
    runTask(timer: TaskTimer, task: () => Promise<void>, taskName?: string): void
}