package usecases.finance_reports.dto

import java.util.UUID

data class DeleteFinanceReportInput(
    val financeReportId: UUID
)