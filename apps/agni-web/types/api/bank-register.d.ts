export type CreateBankRegisterRequest = {
    title: string
    institutionId: string
    accessCode: string
    accounts: {
        accountId?: string    
        bankAccountId: string
        bankName: string
    }[]
}

export type UpdateBankRegisterRequest = {
    title?: string
    accessCode?: string
    accounts?: {
        accountId?: string    
        bankAccountId: string
        bankName: string
    }[]
}

export type GetBankRegisterResponse = {
    id: string
    institutionId: string
    title: string
    active: boolean
    accounts: {
        accountId?: string    
        bankRegisterId: string
        accountName: string
        bankAccountName: string
    }[]
}

