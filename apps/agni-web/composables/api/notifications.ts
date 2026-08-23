import type { NuxtError } from "#app";
import type { Result } from "~/types";
import type { ErrorResponse, ListResponse } from "~/types/api";
import type { GetAllNotification, NotificationQueryFilterRequest } from "~/types/api/notification";
import { listNotificationsResponseToListNotifications } from "~/mappers/notification";
import type { NotificationType } from "~/types/ui/notification";
import type { UseApiFetchReturn } from "~/types/utils";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export default async function useDeleteNotification(notificationId: string) {
    await ApiLinkBuilder
        .route(API_ROUTES.NOTIFICATIONS.DELETE_NOTIFICATION)
        .params({ id: notificationId })
        .execute()
}

export async function fetchNotifications(query: NotificationQueryFilterRequest): Promise<ListResponse<NotificationType>> {
    return await ApiLinkBuilder
        .route(API_ROUTES.NOTIFICATIONS.GET_NOTIFICATIONS)
        .query(query)
        .mapper(listNotificationsResponseToListNotifications)
        .execute()
}

export async function useCheckNotifications(): Promise<Result<boolean>> {
    try {
        const query: NotificationQueryFilterRequest = {
            offset: 0,
            limit: 0,
            queryAll: true,
            isRead: false
        }

        const res = await fetchNotifications(query)

        return { success: true, data: res.items.length > 0}
    } catch(error: any) {
        const nuxtError = error as NuxtError
        return  { success: false, error: nuxtError.data as ErrorResponse}
    }
}

export async function useToggleNotification(notificationId: string) {
    await ApiLinkBuilder
        .route(API_ROUTES.NOTIFICATIONS.TOGGLE_READ)
        .params({ id: notificationId })
        .execute()
}
