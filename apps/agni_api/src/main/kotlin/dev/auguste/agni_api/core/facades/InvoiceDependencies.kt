package dev.auguste.agni_api.core.facades

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.entities.Transaction

class InvoiceDependencies(
    val transactionRepo: IRepository<Transaction>,
    val categoryRepo: IRepository<Category>,
    val budgetRepo: IRepository<Budget>,
    val tagRepo: IRepository<Tag>,
    val accountRepo: IRepository<Account>,
    val deductionRepo: IRepository<Deduction>
)
