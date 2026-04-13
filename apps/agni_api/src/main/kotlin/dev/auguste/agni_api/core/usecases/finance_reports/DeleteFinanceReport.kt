package dev.auguste.agni_api.core.usecases.finance_reports

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.DeleteFinanceReportInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>,
): IUseCase<DeleteFinanceReportInput, Unit> {
    override fun execAsync(input: DeleteFinanceReportInput) {
        val financeReport = financeReportRepo.get(input.financeReportId) ?: throw Error("FinanceReport not found")
        financeReportRepo.delete(input.financeReportId)
    }
}