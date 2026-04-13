package dev.auguste.agni_api.core.usecases.finance_reports.dto

import java.util.UUID

data class DeleteFinanceReportInput(
    val financeReportId: UUID
)