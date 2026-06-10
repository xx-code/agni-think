package dev.auguste.agni_api.core.usecases.finance_reports

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.GetFinanceReportInput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.GetFinanceReportOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>
): IUseCase<GetFinanceReportInput, GetFinanceReportOutput> {
    override fun execAsync(input: GetFinanceReportInput): GetFinanceReportOutput {
        val report: FinanceReport = financeReportRepo.get(input.id) ?: throw dev.auguste.agni_api.core.entities.DomainException.NotFound.FinanceReport(input.id.toString())

        return GetFinanceReportOutput(
            id = report.id,
            title = report.title,
            date = report.date,
            description = report.description,
        )
    }
}