package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.finance_reports.CreateFinanceReport
import dev.auguste.agni_api.core.usecases.finance_reports.DeleteFinanceReport
import dev.auguste.agni_api.core.usecases.finance_reports.GetAllFinanceReport
import dev.auguste.agni_api.core.usecases.finance_reports.GetFinanceReport
import dev.auguste.agni_api.core.usecases.finance_reports.dto.CreateFinanceReportInput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.DeleteFinanceReportInput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.GetFinanceReportInput
import dev.auguste.agni_api.core.usecases.finance_reports.dto.GetFinanceReportOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FinanceReportConfig {

    @Bean
    fun createFinanceReport(
        financeRepo: IRepository<FinanceReport>
    ): IUseCase<CreateFinanceReportInput, CreatedOutput> {
        return CreateFinanceReport(financeRepo)
    }

    @Bean
    fun getFinanceReport(
        financeReportRepo: IRepository<FinanceReport>
    ): IUseCase<GetFinanceReportInput, GetFinanceReportOutput> {
        return GetFinanceReport(financeReportRepo)
    }

    @Bean
    fun getAllFinanceReport(
        financeReportRepo: IRepository<FinanceReport>
    ): IUseCase<QueryFilter, ListOutput<GetFinanceReportOutput>> {
        return GetAllFinanceReport(
            financeReportRepo
        )
    }

    @Bean
    fun deleteFinanceReport(
        financeReportRepo: IRepository<FinanceReport>
    ): IUseCase<DeleteFinanceReportInput, Unit> {
        return DeleteFinanceReport(financeReportRepo)
    }
}