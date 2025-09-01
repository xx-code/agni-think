import type { QueryFilterRequest } from ".";

export type GetAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string   
};

export type GetAllAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string
}

export type GetAllAccountWithPastBalanceResponse = {
    accountId: string
    title: string
    balance: number
    pastBalance: number
    type: string
}

export type GetAllAccountPastBalanceRequest = QueryFilterRequest & {
    period: string 
    periodTime: number
}

export type CreateAccountRequest = {
    title: string 
    type: string
}

export type UpdateAccountRequest = {
    title?: string
    type?: string
}