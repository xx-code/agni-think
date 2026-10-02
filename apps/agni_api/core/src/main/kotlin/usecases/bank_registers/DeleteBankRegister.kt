package usecases.bank_registers

import adapters.repositories.IRepository
import domain.entities.BankRegister
import usecases.bank_registers.dto.DeleteBankRegisterInput
import domain.exceptions.NotFoundException
import usecases.interfaces.IUseCase

class DeleteBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
): IUseCase<DeleteBankRegisterInput, Unit> {
    override fun execAsync(input: DeleteBankRegisterInput) {
        bankRegisterRepo.get(input.bankRegisterId) ?: throw NotFoundException.SingleEntity(input.bankRegisterId, "bank_register")
        bankRegisterRepo.delete(input.bankRegisterId)
    }
}