import type { CalendarDate } from "@internationalized/date"
import type { GetFundResponse } from "../api/fund"

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

export type FundCard = GetFundResponse & { goalSummary?: { numberGoal: number, nextDueDate: Date }  }

export type EditUpdateAmountFundType = {
    accountId: string
    amount: number
}