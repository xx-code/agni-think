package facades

import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Budget
import domain.entities.Category
import domain.entities.Deduction
import domain.entities.Fund
import domain.entities.IncomeSource
import domain.entities.Invoice
import domain.entities.Provision
import domain.entities.ScheduleInvoice
import domain.entities.SpendingPeriodTemplate
import domain.entities.Tag
import domain.entities.Transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FacadesConfig {

    @Bean
    fun dataSeederDependencies(
        accountRepo: IRepository<Account>,
        categoryRepo: IRepository<Category>,
        tagRepo: IRepository<Tag>,
        budgetRepo: IRepository<Budget>,
        deductionsRepo: IRepository<Deduction>,
        invoiceRepo: IRepository<Invoice>,
        transactionRepo: IRepository<Transaction>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        incomeSourceRepo: IRepository<IncomeSource>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>
    ): DataSeederDependencies {
        return DataSeederDependencies(
            accountRepo = accountRepo,
            categoryRepo = categoryRepo,
            tagRepo = tagRepo,
            budgetRepo = budgetRepo,
            deductionsRepo = deductionsRepo,
            invoiceRepo = invoiceRepo,
            transactionRepo = transactionRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            incomeSourceRepo = incomeSourceRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo
        )
    }
}