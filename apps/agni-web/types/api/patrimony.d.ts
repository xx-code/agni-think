import type { QueryFilterRequest } from "."

export type GetPatrimonyResponse = {
    id: string
    title: string
    amount: number
    accountIds: string[]
    currentBalance: number
    pastBalance: number
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
    balance: number
    status: string
    date: string
}

export type UpdateSnapshotPatrimonyRequest = {
    balance?: number
    status?: string
    date?: string
}


export type GetSnapshotPatrimonyResponse = {
    id: string,
    patrimonyId: string
    balance: number
    date: Date
    status: string
}