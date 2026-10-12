package usecases.finance_reports

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.finance_reports.dto.DeleteFinanceReportInput
import domain.exceptions.NotFoundException

class DeleteFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>,
    auth: IAuthenticateAdapter
): UseCase<DeleteFinanceReportInput, Unit>(auth=auth) {
    override suspend fun process(input: DeleteFinanceReportInput) {
        financeReportRepo.get(input.financeReportId) ?: throw NotFoundException.SingleEntity(input.financeReportId, "finance_report")
        financeReportRepo.delete(input.financeReportId)
    }
}