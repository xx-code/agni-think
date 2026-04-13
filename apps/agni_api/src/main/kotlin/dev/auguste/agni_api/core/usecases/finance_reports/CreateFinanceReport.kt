package dev.auguste.agni_api.core.usecases.finance_reports

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.CreateFinanceReportInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>
): IUseCase<CreateFinanceReportInput, CreatedOutput> {
    override fun execAsync(input: CreateFinanceReportInput): CreatedOutput {
        val newReport = FinanceReport(
            title=input.title,
            description = input.description
        )

        financeReportRepo.create(newReport)

        return CreatedOutput(newReport.id)
    }
}