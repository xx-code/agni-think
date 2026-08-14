import type { CalendarDate } from "@internationalized/date"
import type { GetFundResponse } from "../api/fund"

export type EditFund = {
    title: string,
    accountId?: string,
    description: string,
    target: number,
}

export type Fund = GetFundResponse

export type FundCard = GetFundResponse & { goalSummary?: { numberGoal: number, nextDueDate: Date }  }

export type FundContext = Omit<Fund, 'goals'> & { goals: { 
    id: string, 
    title: string, 
    description: string,  
    evaluation: {
        targetAmount: number, 
        currentBalance: number,
        percentage: number,
    }
    dueDate: Date, 
}[] }

export type FundGoalState = 'ACHIEVED' | 'EXPIRED' | 'IN_PROGRESS'

export type FundCardGoal = {
    id: string
    title: string
    description: string
    targetAmount: number
    currentBalance: number
    percentage: number
    dueDate: Date
    status: FundGoalState
}

export type EditUpdateAmountFund = {
    accountId: string
    amount: number
}