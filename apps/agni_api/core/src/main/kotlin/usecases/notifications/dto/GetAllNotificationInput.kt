package usecases.notifications.dto

import adapters.dto.QueryFilter

data class GetAllNotificationInput(
    val queryFilter: QueryFilter
)
