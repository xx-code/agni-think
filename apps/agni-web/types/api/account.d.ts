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
    detailForCreditCard?: GetAllCreditCardDetailResponse 
    detailForBroking?: GetAllBrokingDetailResponse
};

export type GetAllBrokingDetailResponse = {
    managementType: string
    contributionType: string
}

export type GetAllCreditCardDetailResponse = {
    creditCardLimit: number
    creditUtilisation: number
}

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
    detailForCreditCard?: GetAllCreditCardDetailResponse 
    detailForBroking?: GetAllBrokingDetailResponse
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
    creditLimit?: number
    contributionType?: string
    managementType?: string
}