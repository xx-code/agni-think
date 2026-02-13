package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.DeleteNotification
import dev.auguste.agni_api.core.usecases.notifications.GetAllNotifications
import dev.auguste.agni_api.core.usecases.notifications.GetNotification
import dev.auguste.agni_api.core.usecases.notifications.PushNotification
import dev.auguste.agni_api.core.usecases.notifications.ToggleReadNotification
import dev.auguste.agni_api.core.usecases.notifications.dto.DeleteNotificationInput
import dev.auguste.agni_api.core.usecases.notifications.dto.GetNotificationOutput
import dev.auguste.agni_api.core.usecases.notifications.dto.PushNotificationInput
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