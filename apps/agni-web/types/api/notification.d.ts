import type { QueryFilterRequest } from "."

export type GetAllNotification = {
    id: string
    title: string
    content: string
    isRead: boolean
    dateTime: string
}

export type NotificationQueryFilterRequest = QueryFilterRequest & {
    isRead?: boolean
}