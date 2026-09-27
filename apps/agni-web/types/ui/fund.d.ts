import type { CalendarDate } from "@internationalized/date"
import type { GetFundResponse } from "../api/fund"
import type { FundType } from "../constants/fund"

export type EditFund = {
    title: string
    accountId?: string
    type: string
    description: string
    target: number
}

export type Fund = Omit<GetFundResponse, 'type'> & {
    type: FundType
}

export type FundCard = Fund & { goalSummary?: { numberGoal: number, nextDueDate: Date }  }

export type FundContext = Omit<Fund, 'goals' | 'type'> & { goals: { 
    id: string, 
    title: string 
    type: string 
    description: string  
    evaluation: {
        targetAmount: number 
        currentBalance: number
        percentage: number
    }
    dueDate: Date 
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