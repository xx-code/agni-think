package dev.auguste.agni_api.controllers.models

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ApiGetAnalysticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now(),

    )