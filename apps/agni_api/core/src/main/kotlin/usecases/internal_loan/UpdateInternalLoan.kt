package usecases.internal_loan

import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.InternalLoan
import domain.enums.AccountType
import usecases.interfaces.IUseCase
import usecases.internal_loan.dto.UpdateInternalLoanInput

class UpdateInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val accountRepo: IRepository<Account>,
): IUseCase<UpdateInternalLoanInput, Unit> {
    override fun execAsync(input: UpdateInternalLoanInput) {
        val internalLoan = internalLoanRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "internal_loan")

        if (input.fundSourceId != null)  {
            val account = accountRepo.get(input.fundSourceId) ?: throw NotFoundException.SingleEntity(input.fundSourceId, "account")
            val accountType = account.detail.getType()
            if (accountType != _root_ide_package_.domain.enums.AccountType.CHECKING && accountType != _root_ide_package_.domain.enums.AccountType.SAVING)
                throw  ValidationException.InternalLoanAccountNotAllowForCollateral()

            if (account.balance * (0.1) > account.balance)
                throw ValidationException.InternalLoanCollateralBalanceNotAllowed()

            internalLoan.fundSourceId = input.fundSourceId
        }

        if (input.dueDate != null) {
            internalLoan.dueDate = input.dueDate
        }

        if (internalLoan.hasChanged())
            internalLoanRepo.update(internalLoan)
    }
}