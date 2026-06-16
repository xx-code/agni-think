import type { CalendarDate } from "@internationalized/date"

export type EditInternalLoanType = {
    fundSourceId: string
    creditTargetId: string
    transactionDate: CalendarDate
    dueDate: CalendarDate
    transactions: EditTransactionType[] 
    deductions: EditTransactionDeductionType[]
}

export type InternalLoanType = {
    id: string
    fundSourceId: string
    creditTargetId: string
    invoiceId: string
    dueDate: Date
    refundAmount: number
    loanAmount: number
    freezeInvoices: string[]
}