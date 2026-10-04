package facades

import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Budget
import domain.entities.Category
import domain.entities.Deduction
import domain.entities.InternalLoan
import domain.entities.Tag
import domain.entities.Transaction

class InvoiceDependencies(
    val transactionRepo: IRepository<Transaction>,
    val categoryRepo: IRepository<Category>,
    val budgetRepo: IRepository<Budget>,
    val tagRepo: IRepository<Tag>,
    val accountRepo: IRepository<Account>,
    val deductionRepo: IRepository<Deduction>,
    val internalLoanRepo: IRepository<InternalLoan>
)
