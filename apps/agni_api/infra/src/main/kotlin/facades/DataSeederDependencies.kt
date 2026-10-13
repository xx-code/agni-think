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

data class DataSeederDependencies(
    val accountRepo: IRepository<Account>,
    val categoryRepo: IRepository<Category>,
    val tagRepo: IRepository<Tag>,
    val budgetRepo: IRepository<Budget>,
    val deductionsRepo: IRepository<Deduction>,
    val invoiceRepo: IRepository<Invoice>,
    val transactionRepo: IRepository<Transaction>,
    val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    val provisionRepo: IRepository<Provision>,
    val fundRepo: IRepository<Fund>,
    val incomeSourceRepo: IRepository<IncomeSource>,
    val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>
)