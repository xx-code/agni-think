package usecases.notifications

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Notification
import usecases.dto.ListOutput
import usecases.notifications.dto.GetNotificationOutput

class GetAllNotifications(private val notificationRepo: IRepository<Notification>): UseCase<QueryFilter, ListOutput<GetNotificationOutput>>() {

    override suspend fun process(input: QueryFilter): ListOutput<GetNotificationOutput> {
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