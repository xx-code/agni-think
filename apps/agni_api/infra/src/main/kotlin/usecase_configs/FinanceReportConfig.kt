package usecase_configs

import adapters.IAuthenticateAdapter
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
        financeRepo: IRepository<FinanceReport>,
        auth: IAuthenticateAdapter,
    ): UseCase<CreateFinanceReportInput, CreatedOutput> {
        return CreateFinanceReport(financeRepo, auth)
    }

    @Bean
    fun getFinanceReport(
        financeReportRepo: IRepository<FinanceReport>,
        auth: IAuthenticateAdapter,
    ): UseCase<GetFinanceReportInput, GetFinanceReportOutput> {
        return GetFinanceReport(financeReportRepo, auth)
    }

    @Bean
    fun getAllFinanceReport(
        financeReportRepo: IRepository<FinanceReport>,
        auth: IAuthenticateAdapter,
    ): UseCase<QueryFilter, ListOutput<GetFinanceReportOutput>> {
        return GetAllFinanceReport(
            financeReportRepo,
            auth
        )
    }

    @Bean
    fun deleteFinanceReport(
        financeReportRepo: IRepository<FinanceReport>,
        auth: IAuthenticateAdapter,
    ): UseCase<DeleteFinanceReportInput, Unit> {
        return DeleteFinanceReport(financeReportRepo, auth)
    }
}