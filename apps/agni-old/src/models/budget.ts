import { isEmpty } from "@/_utils/isEmpty";
import { Category } from "./category"
import { Tag } from "./tag"

export enum Period {
    YEAR = 'Year',
    MONTH = 'Month',
    WEEK = 'Week',
    DAY = 'Day'
}

const enumLookup: { [key: string]: Period } = {
    "YEAR": Period.YEAR,
    "MONTH": Period.MONTH,
    "WEEK": Period.WEEK,
    "DAY": Period.DAY
};

export function convertStringToPeriod(value: string|null|undefined): Period | string {
    if (isEmpty(value))
        return ''

    let res = enumLookup[value!.toUpperCase()]
    
    if (res)
        return res

    return ''
}

export type Budget = {
    budgetId: string
    name: string
    target: number 
    balance: number
    endDate: string
}
 export type BudgetDetail = {
    id: string,
    title: string,
    target: number,
    categories: Category[],
    tags: Tag[]
    period: string|null
    periodTime: number
    currentBalance: number
    startDate: string
    updateDate: string
    endDate: string|null
 }

