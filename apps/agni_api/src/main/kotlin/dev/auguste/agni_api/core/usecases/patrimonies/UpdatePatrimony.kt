package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.dto.UpdatePatrimonyInput

class UpdatePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>): IUseCase<UpdatePatrimonyInput, Unit> {
    override fun execAsync(input: UpdatePatrimonyInput) {
        val patrimony = patrimonyRepo.get(input.id) ?: throw Error("Patrimony ${input.id} not found")

        if (input.title != null)
            patrimony.title = input.title

        if (input.amount != null)
            patrimony.amount = input.amount

        if (input.accountIds != null) {
            if (input.accountIds.isEmpty())
                if (this.accountRepo.getManyByIds(input.accountIds).size != input.accountIds.size)
                    throw Error("Not all accounts are found")

            patrimony.accountIds = input.accountIds.toMutableSet()
        }

        if (input.type != null) {
            patrimony.type = input.type
        }

        if (patrimony.hasChanged())
            patrimonyRepo.update(patrimony)
    }
}