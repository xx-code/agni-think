package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Notification
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.notifications.DeleteNotification
import usecases.notifications.GetAllNotifications
import usecases.notifications.GetNotification
import usecases.notifications.PushNotification
import usecases.notifications.ToggleReadNotification
import usecases.notifications.dto.DeleteNotificationInput
import usecases.notifications.dto.GetNotificationOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class NotificationConfig {

    @Bean
    fun deleteNotification(
        notificationRepo: IRepository<Notification>,
    ): IUseCase<DeleteNotificationInput, Unit> {
        return DeleteNotification(
            notificationRepo = notificationRepo,
        )
    }

    @Bean
    fun getAllNotifications(
        notificationRepo: IRepository<Notification>,
    ): IUseCase<QueryFilter, ListOutput<GetNotificationOutput>> {
        return GetAllNotifications(
            notificationRepo = notificationRepo,
        )
    }

    @Bean
    fun getNotifications(
        notificationRepo: IRepository<Notification>,
    ): IUseCase<UUID, GetNotificationOutput> {
        return GetNotification(
            notificationRepo = notificationRepo,
        )
    }

    @Bean
    fun pushNotification(
        notificationRepo: IRepository<Notification>,
    ): PushNotification {
        return PushNotification(
            notificationRepo = notificationRepo
        )
    }

    @Bean
    fun togglePushNotification(
        notificationRepo: IRepository<Notification>,
    ): IUseCase<UUID, Unit> {
        return ToggleReadNotification(
            notificationRepo = notificationRepo,
        )
    }
}