import type { CalendarDate } from "@internationalized/date"

export type EditFreezeInvoiceType = {
    accountId: string
    title: string
    amount: number
    status: string
    endDate: CalendarDate
}

export type EditTransfertType = {
    accountIdFrom: string;
    accountIdTo: string;
    date: CalendarDate;
    amount: number;
}

export type TransactionType = {
    id: string
    amount: number
    description: string
    categoryId: string
    tagRefs: string[]
    budgetRefs: string[]
}

export type TransactionDeductionType = {
    id: string
    amount: number
}

export type InvoiceType = {
    id: string
    accountId: string
    date: Date
    type: string
    mouvement: string
    status: string
    subTotal: number
    total: number
    transactions: TransactionType[]
    deductions: TransactionDeductionType[]
}

export type TransactionTableType = {
    id: string
    description: string
    amount: number
    category: {
        id: string
        icon: string
        color: string
        title: string
    } 
    tags: {
        id: string
        value: string
        color: string
    }[]
    budgets: {id: string, title: string}[]
}

export type InvoiceDeductionTableType = {
    name: string
    amount: number
}

export type InvoiceTableType = {
    id: string
    accountId: string
    date: Date
    description: string
    mouvement: string
    type: string
    status: string
    subTotal: number
    total: number
    transactions: TransactionTableType[]
    deductions: InvoiceDeductionTableType[]
}