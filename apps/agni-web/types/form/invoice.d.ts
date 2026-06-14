import type { CalendarDate } from "@internationalized/date"

export type EditTransactionType = {
    amount: number
    description: string
    categoryId: string
    tagIds: string[]
    budgetIds: string[]
} 

export type EditInvoiceDeductionType = {
    deductionId: string
    amount: number
}

export type EditInvoiceType = {
    accountId: string
    state: string
    type: string
    mouvement: string
    date: string
    transactions: EditTransactionType[] 
    deductions: EditInvoiceDeductionType[]
}