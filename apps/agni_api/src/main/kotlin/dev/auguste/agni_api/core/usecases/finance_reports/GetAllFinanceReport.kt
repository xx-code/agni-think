package dev.auguste.agni_api.core.usecases.finance_reports

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.GetFinanceReportOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllFinanceReport(
    private val reportRepo: IRepository<FinanceReport>
): IUseCase<QueryFilter, ListOutput<GetFinanceReportOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetFinanceReportOutput> {
        val reports = reportRepo.getAll(query = input)

        return ListOutput(
            items = reports.items.map {
                GetFinanceReportOutput(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    date = it.date,
                )
            },
            total = reports.total
        )
    }
}