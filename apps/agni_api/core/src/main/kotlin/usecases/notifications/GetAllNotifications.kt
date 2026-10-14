package usecases.notifications

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Notification
import usecases.dto.ListOutput
import usecases.notifications.dto.GetAllNotificationInput
import usecases.notifications.dto.GetNotificationOutput

class GetAllNotifications(
    private val notificationRepo: IRepository<Notification>,
    auth: IAuthenticateAdapter
): UseCase<GetAllNotificationInput, ListOutput<GetNotificationOutput>>(auth = auth) {

    override suspend fun process(input: GetAllNotificationInput): ListOutput<GetNotificationOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Notification>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val notifications = notificationRepo.getAll(input.queryFilter, condition)

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