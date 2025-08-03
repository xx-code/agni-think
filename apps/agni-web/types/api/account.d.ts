export type GetAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string   
};

export type GetAccountWithPastInfoResponse = GetAccountResponse & {
    pastBalanceDetail: {
        balance: number,
        diffPercent: number,
        doIncrease: boolean
    }
}

export type GetAllAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string
}

export type CreateAccountRequest = {
    title: string 
    type: string
}

export type UpdateAccountRequest = {
    id: string
    title?: string
    type?: string
}