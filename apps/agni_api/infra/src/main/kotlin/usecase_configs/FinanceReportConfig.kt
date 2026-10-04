package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinanceReport
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.finance_reports.CreateFinanceReport
import usecases.finance_reports.DeleteFinanceReport
import usecases.finance_reports.GetAllFinanceReport
import usecases.finance_reports.GetFinanceReport
import usecases.finance_reports.dto.CreateFinanceReportInput
import usecases.finance_reports.dto.DeleteFinanceReportInput
import usecases.finance_reports.dto.GetFinanceReportInput
import usecases.finance_reports.dto.GetFinanceReportOutput
import usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase

@Configuration
class FinanceReportConfig {

    @Bean
    fun createFinanceReport(
        financeRepo: IRepository<FinanceReport>
    ): UseCase<CreateFinanceReportInput, CreatedOutput> {
        return CreateFinanceReport(financeRepo)
    }

    @Bean
    fun getFinanceReport(
        financeReportRepo: IRepository<FinanceReport>
    ): UseCase<GetFinanceReportInput, GetFinanceReportOutput> {
        return GetFinanceReport(financeReportRepo)
    }

    @Bean
    fun getAllFinanceReport(
        financeReportRepo: IRepository<FinanceReport>
    ): UseCase<QueryFilter, ListOutput<GetFinanceReportOutput>> {
        return GetAllFinanceReport(
            financeReportRepo
        )
    }

    @Bean
    fun deleteFinanceReport(
        financeReportRepo: IRepository<FinanceReport>
    ): UseCase<DeleteFinanceReportInput, Unit> {
        return DeleteFinanceReport(financeReportRepo)
    }
}