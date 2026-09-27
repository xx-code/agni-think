import type { QueryFilterRequest } from "."
import type { FundType } from "../constants/fund"

export type GetFundResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    type: FundType
    accountId?: string
    goals: {
        id: string,
        title: string,
        dueDate: string
    }[]
}

export type CreateFundRequest = {
    target: number;
    title: string;
    accountId?: string
    description: string
    type: FundType
}

export type UpdateFundRequest = {
    target?: number
    title?: string
    accountId?: string
    description?: string
    type?: FundType
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