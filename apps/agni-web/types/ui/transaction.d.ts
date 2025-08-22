import type { CalendarDate } from "@internationalized/date"

export type EditTransactionType = {
    accountId: string
    amount: number
    date: CalendarDate
    description: string
    type: string
    categoryId: string
    tagIds: string[]
    budgetIds: string[]
}

export type EditFreezeTransactionType = {
    accountId: string,
    amount: number,
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
    accountId: string
    amount: number
    date: Date
    description: string
    recordType: string
    type: string
    status: string
    categoryId: string 
    tagIds: string[]
    budgetIds: string[]
}

export type TransactionTableType = {
    id: string
    accountId: string
    amount: number
    date: Date
    description: string
    recordType: string
    type: string
    status: string
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