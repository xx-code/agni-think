import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAllNotification, NotificationQueryFilterRequest } from "~/types/api/notification";
import type { NotificationType } from "~/types/ui/notification";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useNotification(query: Reactive<NotificationQueryFilterRequest>): UseApiFetchReturn<ListResponse<NotificationType>> {
    const {data, error, status, refresh} = useFetch('/api/notifications', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllNotification>) =>  {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    title: i.title,
                    isRead: i.isRead,
                    content: i.content,
                    dateTime: new Date(i.dateTime)
                })),
                totals: data.totals
            }
        }
    })

    return { data, error, refresh, status}
}


export async function fetchNotifications(query: NotificationQueryFilterRequest): Promise<ListResponse<NotificationType>> {
    const res = await $fetch<ListResponse<GetAllNotification>>('/api/notifications', {
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
        totals: res.totals
    }
}

export async function checkNotifications(): Promise<boolean> {
    const query: NotificationQueryFilterRequest = {
        offset: 0,
        limit: 0,
        isRead: false
    }
    const res = await $fetch<ListResponse<GetAllNotification>>('/api/notifications', {
        method: 'GET',
        query: query
    })

    return res.items.length > 0
}