import type { QueryFilterRequest } from "."
import type { FundType } from "../constants/fund"

export type SinkingFundDetailResponse = {
    numberOfContributions: number
    startDate: string
    contributionRecurrence: {
        interval: number
        period: string
    }
    fundScheduler?: {
        date: string
        repeater?: {
            interval: number
            period: string   
        }
    }
    isClosed: boolean
}

export type GetFundResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    type: string
    accountId?: string
    goals: {
        id: string,
        title: string,
        dueDate: string
    }[]
    sinkingFundDetail?: SinkingFundDetailResponse | null
}

export type SinkFundRequest = {
    startDate: string
    numberOfContributions: number
    contributionRecurrence: {
        interval: number
        period: string
    }  
    scheduler?: {
        date: string
        repeater?: {
            interval: number
            period: string   
        }
    }
}

export type CreateFundRequest = {
    target: number;
    title: string;
    accountId?: string
    description: string
    type: FundType
    sinkingFundDetail?: SinkFundRequest
}

export type UpdateFundRequest = {
    target?: number
    title?: string
    accountId?: string
    description?: string
    type?: FundType
    sinkingFundDetail?: SinkFundRequest
}

export type UpgradeFundRequest = {
    accountId: string
    amount: number
}

export type DeleteFundRequest = {
    accountId?: string
}

export type QueryFilterFundRequest = QueryFilterRequest & {
    type?: FundType
}