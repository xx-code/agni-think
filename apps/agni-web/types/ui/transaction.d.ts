import type { CalendarDate } from "@internationalized/date"

export type EditRecordType = {
    amount: number
    description: string
    categoryId: string
    tagIds: string[]
    budgetIds: string[]
} 

export type EditTransactionDeductionType = {
    deductionId: string
    amount: number
}

export type EditTransactionType = {
    accountId: string
    state: string
    type: string
    date: CalendarDate
    records: EditRecordType[] 
    deductions: EditTransactionDeductionType[]
}

export type EditFreezeTransactionType = {
    accountId: string,
    title: string
    amount: number,
    endDate: CalendarDate
}

export type EditTransfertType = {
    accountIdFrom: string;
    accountIdTo: string;
    date: CalendarDate;
    amount: number;
}

export type RecordType = {
    id: string
    amount: number
    description: string
    type: string
    categoryId: string
    tagRefs: string[]
    budgetRefs: string[]
}

export type TransactionDeductionType = {
    id: string
    amount: number
}

export type TransactionType = {
    id: string
    accountId: string
    date: Date
    type: string
    status: string
    subTotal: number
    total: number
    records: RecordType[]
    deductions: TransactionDeductionType[]
}

export type RecordTableType = {
    id: string
    description: string
    type: string
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

export type TransactionDeductionTableType = {
    name: string
    amount: number
}

export type TransactionTableType = {
    id: string
    accountId: string
    date: Date
    description: string
    type: string
    status: string
    subTotal: number
    total: number
    records: RecordTableType[]
    deductions: TransactionDeductionTableType[]
}