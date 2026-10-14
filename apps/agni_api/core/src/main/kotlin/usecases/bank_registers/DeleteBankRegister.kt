package usecases.bank_registers

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.BankRegister
import usecases.bank_registers.dto.DeleteBankRegisterInput
import domain.exceptions.NotFoundException
class DeleteBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
    auth: IAuthenticateAdapter
): UseCase<DeleteBankRegisterInput, Unit>(auth = auth) {
    override suspend fun process(input: DeleteBankRegisterInput) {
        bankRegisterRepo.get(input.bankRegisterId) ?: throw NotFoundException.SingleEntity(input.bankRegisterId, "bank_register")
        bankRegisterRepo.delete(input.bankRegisterId)
    }
}