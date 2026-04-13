package dev.auguste.agni_api.core.usecases.bank_registers

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.usecases.bank_registers.dto.DeleteBankRegisterInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
): IUseCase<DeleteBankRegisterInput, Unit> {
    override fun execAsync(input: DeleteBankRegisterInput) {
        bankRegisterRepo.get(input.bankRegisterId) ?: throw Error("Bank Register ID ${input.bankRegisterId} not found")
        bankRegisterRepo.delete(input.bankRegisterId)
    }
}