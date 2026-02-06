package dev.auguste.agni_api.core.usecases.notifications

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.dto.GetNotificationOutput

class GetAllNotifications(val notificationRepo: IRepository<Notification>): IUseCase<QueryFilter, ListOutput<GetNotificationOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetNotificationOutput> {
        val notifications = notificationRepo.getAll(input)

        return ListOutput(
            items = notifications.items.map {
                GetNotificationOutput(
                    id = it.id,
                    title = it.title,
                    content = it.content,
                    isRead = it.isRead,
                    dateTime = it.dateTime
                )
            },
            total = notifications.total
        )
    }
}