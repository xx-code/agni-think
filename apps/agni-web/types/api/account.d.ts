export type GetAccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string   
};

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
    title?: string
    type?: string
}