import type {  CalendarDate } from "@internationalized/date"

export type TypePatrimony = 'Asset' | 'Liability' 

export type EditePatrimony = {
    title: string
    description: string
    categoryId: string
    accountIds: string[]
    amount: number
    type: TypePatrimony
}

export type EditSnapshotPatrimony = {
    balance: number
    date: CalendarDate
    status: string
}

export type PatrimonyType = {
    id: string
    title: string
    amount: number
    lastSnapshotBalance: number
    currentBalance: number 
    accountIds: string[]
    type: TypePatrimony
}

export type SnapshotPatrimonyType = {
    id: string
    patrimonyId: string
    balance: number
    date: Date
    status: string
}