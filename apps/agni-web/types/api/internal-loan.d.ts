export type CreateInternalLoanRequest = {
    fundAccountId: string
    creditAccountId: string
    transactionDate: string
    dueDate: string
    transactions: {
        amount: number
        categoryId: string
        description: string
        tagIds: string[]
        budgetIds: string[]
    }[]
    deductions: {
        deductionId: string
        amount: number
    }[]
}

export type UpdateInternalLoanRequest = {
    fundAccountId?: string
    dueDate?: string
}

export type GetInternalLoanResponse = {
    id: string
    creditTargetId: string
    invoiceId: string
    fundSourceId: string
    dueDate: string
}