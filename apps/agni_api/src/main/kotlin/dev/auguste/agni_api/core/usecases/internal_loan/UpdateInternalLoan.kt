package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.UpdateInternalLoanInput

class UpdateInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val accountRepo: IRepository<Account>,
): IUseCase<UpdateInternalLoanInput, Unit> {
    override fun execAsync(input: UpdateInternalLoanInput) {
        val internalLoan = internalLoanRepo.get(input.id) ?: throw DomainException.NotFound.InternalLoan(input.id)

        if (input.fundSourceId != null)  {
            val account = accountRepo.get(input.fundSourceId) ?: throw DomainException.NotFound.Account(input.fundSourceId)
            val accountType = account.detail.getType()
            if (accountType != AccountType.CHECKING && accountType != AccountType.SAVING)
                throw  DomainException.BusinessLogic.InternalLoanAccountNotAllowForCollateral()

            if (account.balance * (0.1) > account.balance)
                throw DomainException.BusinessLogic.Validation("Balance collateral not allowed")

            internalLoan.fundSourceId = input.fundSourceId
        }

        if (input.dueDate != null) {
            internalLoan.dueDate = input.dueDate
        }

        if (internalLoan.hasChanged())
            internalLoanRepo.update(internalLoan)
    }
}