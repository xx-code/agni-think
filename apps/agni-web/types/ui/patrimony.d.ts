import type {  CalendarDate } from "@internationalized/date"
import type { TypePatrimony } from "../constants/patrimony"
import type { GetPatrimonyResponse } from "../api/patrimony"


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

export type PatrimonyType = Omit<GetPatrimonyResponse, 'type'> & {
    evolution: number
    type: TypePatrimony
}

export type SnapshotPatrimonyType = {
    id: string
    patrimonyId: string
    balance: number
    date: Date
    status: string
}

export type PatrimonyCard = {
    id: string
    title: string
    description: string
    balance: number
    evolution: number,
    isFund: boolean
    type: TypePatrimony
}