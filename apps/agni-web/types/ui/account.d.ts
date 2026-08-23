import type { CalendarDate } from "@internationalized/date"
import type { AccountType } from "../constants/account"
import type { GetAccountResponse, GetAccountWithDetailResponse, GetCreditCardDetailResponse, GetCheckingDetailResponse } from "../api/account"

export type EditAccount = {
    title: string
    type: string
    color: string
    creditLimit?: number
    contributionType?: string
    managementType?: string
    invoiceDate?: CalendarDate
}

export type Account = Omit<GetAccountResponse, 'type'> & {
    type: AccountType
}

export type AccountWithDetailType = Omit<GetAccountWithDetailResponse, 'detail'> & {
    detail?: {
        detailForCreditCard?: AccountCreditDetailType
        detailForBroking?: AccountBrokeDetailType
        detailForChecking?: AccountCheckingDetailType
    }
}

export type AccountWithPastBalanceType = Account & {
    pastBalanceDetail: {
        balance: number,
        diffPercent: number,
        doIncrease: boolean
    }
}

export type AccountCreditDetailType = Omit<GetCreditCardDetailResponse, 'nextInvoicePayment'> & {
    nextInvoicePayment: Date
}

export type AccountBrokeDetailType = GetBrokingDetailResponse

export type AccountCheckingDetailType = GetCheckingDetailResponse

export type AccountCard = {
    id: string
    type: string
    title: string
    color: string
    balance: number
    balanceHistory: number[]
}

export type SlideQuickViewTransactionType = {
    id: string
    icon: string
    color: string
    category: string
    description: string
    status: string
    type: string
    date: Date
    subTotal: number
    total: number 
}

export type QuickViewTransactionBalanceInfo = AccountWithDetailType