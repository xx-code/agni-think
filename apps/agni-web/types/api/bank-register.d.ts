export type CreateBankRegisterRequest = {
    title: string
    accessCode: string
    accounts: {
        accountId: string    
        bankAccountId: string
    }[]
}

export type UpdateBankRegisterRequest = {
    title?: string
    accessCode?: string
    accounts?: {
        accountId: string    
        bankAccountId: string
    }[]
}

export type GetBankRegisterResponse = {
    bankRegisterId: string
    title: string
    isActive: boolean
    accounts: {
        accountId: string    
        bankAccountId: string
        accountName: string
    }[]
}

