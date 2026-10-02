package usecases.patrimonies

import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Patrimony
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import usecases.patrimonies.dto.UpdatePatrimonyInput

class UpdatePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>): IUseCase<UpdatePatrimonyInput, Unit> {
    override fun execAsync(input: UpdatePatrimonyInput) {
        val patrimony = patrimonyRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "patrimony")

        if (input.title != null)
            patrimony.title = input.title

        if (input.amount != null)
            patrimony.amount = input.amount

        if (input.accountIds != null) {
            if (input.accountIds.isEmpty())
                if (this.accountRepo.getManyByIds(input.accountIds).size != input.accountIds.size)
                    throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.accountIds.joinToString()), "account")

            patrimony.accountIds = input.accountIds.toMutableSet()
        }

        if (input.type != null) {
            patrimony.type = input.type
        }

        if (patrimony.hasChanged())
            patrimonyRepo.update(patrimony)
    }
}