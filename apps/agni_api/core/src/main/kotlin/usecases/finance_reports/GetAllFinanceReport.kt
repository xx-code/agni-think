package usecases.finance_reports

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.dto.ListOutput
import usecases.finance_reports.dto.GetFinanceReportOutput
class GetAllFinanceReport(
    private val reportRepo: IRepository<FinanceReport>
): UseCase<QueryFilter, ListOutput<GetFinanceReportOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetFinanceReportOutput> {
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