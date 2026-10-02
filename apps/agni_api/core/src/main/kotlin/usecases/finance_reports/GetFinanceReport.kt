package usecases.finance_reports

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.FinanceReport
import usecases.finance_reports.dto.GetFinanceReportInput
import usecases.finance_reports.dto.GetFinanceReportOutput
import usecases.interfaces.IUseCase

class GetFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>
): IUseCase<GetFinanceReportInput, GetFinanceReportOutput> {
    override fun execAsync(input: GetFinanceReportInput): GetFinanceReportOutput {
        val report: FinanceReport = financeReportRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "finance_report")

        return GetFinanceReportOutput(
            id = report.id,
            title = report.title,
            date = report.date,
            description = report.description,
        )
    }
}