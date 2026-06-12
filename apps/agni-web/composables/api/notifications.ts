import type { NuxtError } from "#app";
import type { Result } from "~/types";
import type { ErrorResponse, ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAllNotification, NotificationQueryFilterRequest } from "~/types/api/notification";
import type { NotificationType } from "~/types/ui/notification";
import type { UseApiFetchReturn } from "~/types/utils";

export default async function useDeleteNotification(notificationId: string) {
    await $fetch(`api/notifications/${notificationId}`, {
        method: 'DELETE'
    })
} 

export async function fetchNotifications(query: NotificationQueryFilterRequest): Promise<ListResponse<NotificationType>> {
    const res = await $fetch<ListResponse<GetAllNotification>>(`api/notifications`, {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(i => ({ 
            id: i.id, 
            title: i.title,
            isRead: i.isRead,
            content: i.content,
            dateTime: new Date(i.dateTime)
        })),
        total: Number(res.total) 
    }
}

export async function useCheckNotifications(): Promise<Result<boolean>> {
    try {
        const query: NotificationQueryFilterRequest = {
            offset: 0,
            limit: 0,
            queryAll: true,
            isRead: false
        }
        const res = await $fetch<ListResponse<GetAllNotification>>(`api/notifications`, {
            method: 'GET',
            query: query
        })

        return { success: true, data: res.items.length > 0} 
    } catch(error: any) {
        const nuxtError = error as NuxtError
        return  { success: false, error: nuxtError.data as ErrorResponse}
    } 
}

export async function useToggleNotification(notificationId: string) {
    await $fetch(`api/notifications/${notificationId}/toggle-read`, {
        method: 'PUT'
    })
} 