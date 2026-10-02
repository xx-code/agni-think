package usecases.notifications

import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Notification
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.notifications.dto.GetNotificationOutput

class GetAllNotifications(private val notificationRepo: IRepository<Notification>): IUseCase<QueryFilter, ListOutput<GetNotificationOutput>> {

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