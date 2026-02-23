package dev.auguste.agni_api.core.usecases.analystics.dto

import java.time.LocalDateTime

data class GetSavingBalanceInput(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)
