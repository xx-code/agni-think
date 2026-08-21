package dev.auguste.agni_api.core.usecases.invoices.dto

import java.time.LocalDate

data class GetBalanceByPeriodOutput(
    val date: LocalDate,
    val balance: Double,
    val income: Double,
    val spend: Double
)
