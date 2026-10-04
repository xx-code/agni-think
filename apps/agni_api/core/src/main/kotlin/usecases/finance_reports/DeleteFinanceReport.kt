package usecases.finance_reports

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.finance_reports.dto.DeleteFinanceReportInput
import domain.exceptions.NotFoundException

class DeleteFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>,
): UseCase<DeleteFinanceReportInput, Unit>() {
    override suspend fun process(input: DeleteFinanceReportInput) {
        financeReportRepo.get(input.financeReportId) ?: throw NotFoundException.SingleEntity(input.financeReportId, "finance_report")
        financeReportRepo.delete(input.financeReportId)
    }
}