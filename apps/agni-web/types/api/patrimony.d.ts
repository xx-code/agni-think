export type GetPatrimonyResponse = {
    id: string
    title: string
    accounts: {
        accountId: string
        title: string
        balance: number
    }[],
    trackBalance: number
    type: string
}

export type GetAllPatrimoniesResponse = {
    id: string
    title: string
    accountBalance: number
    lastBalance: number
    type: string
}

export type CreatePatrimonyRequest = {
    title: string
    accountIds?: string[]
    type: string
}

export type UpdatePatrimonyRequest = {
    title?: string
    accountIds?: string[]
    type?: string
}

export type AddSnapshotPatrimonyRequest = {
    balance: number
    status: string
    date: Date
}

export type UpdateSnapshotPatrimonyRequest = {
    patrimonyId?: string
    balance?: number
    status?: string
    date?: Date
}

export type GetAllSnapshotPatrimonyRequest = {
    startDate?: string
    endDate?: string
}

export type GetAllSnapshotPatrimonyResponse = {
    balance: number
    date: Date
    status: string
}