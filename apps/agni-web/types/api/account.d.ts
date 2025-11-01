import type { QueryFilterRequest } from ".";

export type GetAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string   
};

export type GetAccountWithDetailResponse = {
    accountId: string
    title: string
    balance: number
    type: string   
    lockedBalance: number
    freezedBalance: number
};

export type GetAllAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string
}

export type GetallAccountWithDetailResponse = {
    accountId: string
    title: string
    balance: number
    type: string   
    lockedBalance: number
    freezedBalance: number
};

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