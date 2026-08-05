import type { CalendarDate } from "@internationalized/date"

export type EditFundType = {
    title: string,
    accountId?: string,
    description: string,
    target: number,
}

export type FundType = {
    id: string,
    accountId?: string
    title: string,
    description: string,
    target: number,
    balance: number
}

export type EditUpdateAmountFundType = {
    accountId: string
    amount: number
}