package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.UpdateInternalLoanInput

class UpdateInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val accountRepo: IRepository<Account>,
): IUseCase<UpdateInternalLoanInput, Unit> {
    override fun execAsync(input: UpdateInternalLoanInput) {
        val internalLoan = internalLoanRepo.get(input.id) ?: throw Exception("Internal loan with id ${input.id} not found")

        if (input.fundSourceId != null)  {
            val account = accountRepo.get(input.fundSourceId) ?: throw Exception("Account not found")
            val accountType = account.detail.getType()
            if (accountType != AccountType.CHECKING && accountType != AccountType.SAVING)
                throw  Exception("Account type not allow for loan collateral")

            if (account.balance * (0.1) > account.balance)
                throw Exception("Balance collateral not allowed")

            internalLoan.fundSourceId = input.fundSourceId
        }

        if (input.dueDate != null) {
            internalLoan.dueDate = input.dueDate
        }

        if (internalLoan.hasChanged())
            internalLoanRepo.update(internalLoan)
    }
}