package usecases.finance_reports

import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.CreatedOutput
import usecases.finance_reports.dto.CreateFinanceReportInput
import usecases.interfaces.IUseCase

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