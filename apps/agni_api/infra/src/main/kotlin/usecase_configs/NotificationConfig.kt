package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Notification
import domain.entities.User
import usecases.dto.ListOutput
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
import usecases.UseCase
import usecases.notifications.dto.GetAllNotificationInput
import java.util.UUID

@Configuration
class NotificationConfig {

    @Bean
    fun deleteNotification(
        notificationRepo: IRepository<Notification>,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteNotificationInput, Unit> {
        return DeleteNotification(
            notificationRepo = notificationRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllNotifications(
        notificationRepo: IRepository<Notification>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllNotificationInput, ListOutput<GetNotificationOutput>> {
        return GetAllNotifications(
            notificationRepo = notificationRepo,
            auth = auth
        )
    }

    @Bean
    fun getNotifications(
        notificationRepo: IRepository<Notification>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetNotificationOutput> {
        return GetNotification(
            notificationRepo = notificationRepo,
            auth = auth
        )
    }

    @Bean
    fun pushNotification(
        notificationRepo: IRepository<Notification>,
        auth: IAuthenticateAdapter
    ): PushNotification {
        return PushNotification(
            notificationRepo = notificationRepo,
            auth = auth
        )
    }

    @Bean
    fun togglePushNotification(
        notificationRepo: IRepository<Notification>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, Unit> {
        return ToggleReadNotification(
            notificationRepo = notificationRepo,
            auth = auth
        )
    }
}