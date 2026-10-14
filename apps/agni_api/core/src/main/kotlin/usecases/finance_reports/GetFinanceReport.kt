package usecases.finance_reports

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.FinanceReport
import usecases.finance_reports.dto.GetFinanceReportInput
import usecases.finance_reports.dto.GetFinanceReportOutput
class GetFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>,
    auth: IAuthenticateAdapter
): UseCase<GetFinanceReportInput, GetFinanceReportOutput>(auth=auth) {
    override suspend fun process(input: GetFinanceReportInput): GetFinanceReportOutput {
        val report: FinanceReport = financeReportRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "finance_report")

        return GetFinanceReportOutput(
            id = report.id,
            title = report.title,
            date = report.date,
            description = report.description,
        )
    }
}