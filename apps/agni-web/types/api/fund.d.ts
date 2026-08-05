import type { QueryFilterRequest } from "."

export type GetFundResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    accountId?: string
}

export type CreateFundRequest = {
    target: number;
    title: string;
    accountId?: string
    description: string
}

export type UpdateFundRequest = {
    target?: number
    title?: string
    accountId?: string
    description?: string
}

export type UpgradeFundRequest = {
    accountId: string
    amount: number
}

export type DeleteFundRequest = {
    accountId?: string
}

export type QueryFilterFundRequest = QueryFilterRequest & {
}