package dev.auguste.agni_api.controllers.models

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import java.util.UUID

data class ApiGetBudgetingRuleModel(
    val period: String?,
    val interval: Int = 0,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime?,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val endDate: LocalDateTime?
)

data class ApiGetSavingAnalyticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now()
)

data class ApiGetCategoryAnalyticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now(),

    val offset: Int = 0,
    val limit: Int = 10,
    val queryAll: Boolean = false

    )

data class ApiGetTagAnalyticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now(),

    val offset: Int = 0,
    val limit: Int = 10,
    val queryAll: Boolean = false,
    val categoryId: UUID? = null
)