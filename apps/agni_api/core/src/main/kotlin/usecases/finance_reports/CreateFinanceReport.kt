package usecases.finance_reports

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.dto.CreatedOutput
import usecases.finance_reports.dto.CreateFinanceReportInput
class CreateFinanceReport(
    private val financeReportRepo: IRepository<FinanceReport>
): UseCase<CreateFinanceReportInput, CreatedOutput>() {
    override suspend fun process(input: CreateFinanceReportInput): CreatedOutput {
        val newReport = FinanceReport(
            title=input.title,
            description = input.description
        )

        financeReportRepo.create(newReport)

        return CreatedOutput(newReport.id)
    }
}