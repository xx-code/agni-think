package usecases.finance_reports

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.ListOutput
import usecases.finance_reports.dto.GetFinanceReportOutput
import usecases.interfaces.IUseCase

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