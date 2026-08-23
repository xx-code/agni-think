import type { ListResponse } from "~/types/api";
import type { GetAllNotification } from "~/types/api/notification";
import type { NotificationType } from "~/types/ui/notification";

export function notificationResponseToNotification(data: GetAllNotification): NotificationType {
    return {
        id: data.id,
        title: data.title,
        content: data.content,
        isRead: data.isRead,
        dateTime: new Date(data.dateTime)
    }
}

export function listNotificationsResponseToListNotifications(data: ListResponse<GetAllNotification>): ListResponse<NotificationType> {
    return {
        items: data.items.map(i => notificationResponseToNotification(i)),
        total: Number(data.total)
    }
}
