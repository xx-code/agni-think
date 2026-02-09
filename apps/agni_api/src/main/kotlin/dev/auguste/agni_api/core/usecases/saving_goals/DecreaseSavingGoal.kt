package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DecreaseSavingGoalInput
import java.time.LocalDateTime
import java.util.Date

class DecreaseSavingGoal(
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<DecreaseSavingGoalInput, Unit> {
    override fun execAsync(input: DecreaseSavingGoalInput) {
        unitOfWork.execute {
            val savingGoal = savingGoalRepo.get(input.savingGoalId) ?: throw Error("Could not find saving goal")

            if (input.amount <= 0)
                throw Error("Amount must be greater than zero")

            if (savingGoal.balance < input.amount)
                throw Error("Balance must be greater than amount.")

            val accountId = if (savingGoal.accountId == null) {
                if (input.accountId == null)
                    throw Error("Account ID must be non-null.")
                input.accountId
            } else {
                if (savingGoal.accountId == null)
                    throw Error("Account ID Link to saving is null.")
                savingGoal.accountId!!
            }


            val account = accountRepo.get(accountId) ?: throw Error("Account not found.")
            if (account.balance < input.amount)
                throw Error("Balance must be greater than amount.")

            createInvoice.execInnerAsync(CreateInvoiceInput(
                accountId = accountId,
                status = InvoiceStatusType.COMPLETED,
                date = LocalDateTime.now(),
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.CREDIT,
                currency = null,
                transactions = setOf(TransactionInput(
                    amount = input.amount,
                    categoryId = SAVING_CATEGORY_ID,
                    description = "Argent plan d'epargne ${savingGoal.title}",
                    tagIds = setOf(),
                    budgetIds = setOf()
                )),
                deductions = setOf()
            ))

            savingGoal.balance -= input.amount
            savingGoalRepo.update(savingGoal)
        }
    }
}