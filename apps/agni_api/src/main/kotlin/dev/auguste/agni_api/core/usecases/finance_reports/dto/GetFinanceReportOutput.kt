package dev.auguste.agni_api.core.usecases.finance_reports.dto

import java.time.LocalDate
import java.util.UUID

data class GetFinanceReportOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val date: LocalDate,
)