import type { QueryFilterRequest } from ".";

export type GetAccountResponse = {
    id: string
    title: string
    balance: number
    type: string   
};

export type GetAccountWithDetailResponse = {
    id: string
    title: string
    balance: number
    type: string   
    lockedBalance: number
    freezeBalance: number
    detail: {
        detailForCreditCard?: GetCreditCardDetailResponse 
        detailForBroking?: GetBrokingDetailResponse
        detailForChecking?: GetCheckingDetailResponse
    }
};

export type GetCheckingDetailResponse = {
    buffer: number
}

export type GetBrokingDetailResponse = {
    managementType: string
    contributionType: string
}

export type GetCreditCardDetailResponse = {
    creditCardLimit: number
    creditUtilisation: number
}

export type CreateAccountRequest = {
    title: string 
    type: string
    currencyId?: string
    detail: AccountDetailRequest
}

export type UpdateAccountRequest = {
    title?: string
    type?: string
    currencyId?: string
    detail?: AccountDetailRequest
}

export type AccountDetailRequest = {
    creditLimit?: number
    contributionType?: string
    managementAccountType?: string
    buffer?: number
    secureAmount?: number
}