package usecases.finance_reports

import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.finance_reports.dto.DeleteFinanceReportInput
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException

class DeleteFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>,
): IUseCase<DeleteFinanceReportInput, Unit> {
    override fun execAsync(input: DeleteFinanceReportInput) {
        val financeReport = financeReportRepo.get(input.financeReportId) ?: throw NotFoundException.SingleEntity(input.financeReportId, "finance_report")
        financeReportRepo.delete(input.financeReportId)
    }
}