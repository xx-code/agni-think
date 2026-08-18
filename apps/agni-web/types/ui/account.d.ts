import type { CalendarDate } from "@internationalized/date"

export type EditAccount = {
    title: string
    type: string
    color: string
    creditLimit?: number
    contributionType?: string
    managementType?: string
    invoiceDate?: CalendarDate
}

export type Account = {
    id: string
    title: string
    type: string
    color: string
    balance: number
}

export type AccountWithPastBalanceType = Account & {
    pastBalanceDetail: {
        balance: number,
        diffPercent: number,
        doIncrease: boolean
    }
}

export type AccountCreditDetailType = {
    creditUtilisation: number
    creditLimit: number
    nextInvoicePaymentDate: Date
}

export type AccountBrokeDetailType = {
    managementType: string
    type: string
}

export type AccountCheckingDetailType = {
    buffer: number
}

export type AccountWithDetailType = Account & {
    lockedBalance: number
    freezedBalance: number
    detail?: AccountCreditDetailType | AccountBrokeDetailType | AccountCheckingDetailType
}

export type AccountCard = {
    id: string
    type: string
    title: string
    color: string
    balance: number
    balanceHistory: number[]
}