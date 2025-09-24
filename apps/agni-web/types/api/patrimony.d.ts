import type { QueryFilterRequest } from "."

export type GetPatrimonyResponse = {
    id: string
    title: string
    amount: number
    accounts: {
        accountId: string
        title: string
        balance: number
        pastBalance: number
    }[],
    currentSnapshotBalance: number
    pastSnapshotBalance: number
    type: string
}

export type GetAllPatrimoniesResponse = {
    id: string
    title: string
    amount: number
    currentSnapshotBalance: number
    lastSnapshotBalance: number
    type: string
}

export type CreatePatrimonyRequest = {
    title: string
    amount: number
    accountIds?: string[]
    type: string
}

export type UpdatePatrimonyRequest = {
    title?: string
    amount?: number
    accountIds?: string[]
    type?: string
}

export type AddSnapshotPatrimonyRequest = {
    patrimonyId: string
    balance: number
    status: string
    date: string
}

export type UpdateSnapshotPatrimonyRequest = {
    patrimonyId?: string
    balance?: number
    status?: string
    date?: string
}

export type GetAllSnapshotPatrimonyRequest = QueryFilterRequest & {
    period: string
    periodTime: number
}

export type GetAllPatrimonyRequest = QueryFilterRequest & {
    period: string
    periodTime: number
}

export type GetPatrimonyRequest = {
    period: string
    periodTime: number
}

export type GetAllSnapshotPatrimonyResponse = {
    id: string,
    patrimonyId: string
    balance: number
    date: Date
    status: string
}