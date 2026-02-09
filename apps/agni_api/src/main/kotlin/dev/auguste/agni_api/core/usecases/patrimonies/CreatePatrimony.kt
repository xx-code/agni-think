package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.dto.CreatePatrimonyInput
import java.time.LocalDate
import java.util.Date

class CreatePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>,
    private val snapshotRepo: IRepository<PatrimonySnapshot>,
    private val unitOfWork: IUnitOfWork): IUseCase<CreatePatrimonyInput, CreatedOutput> {

    override fun execAsync(input: CreatePatrimonyInput): CreatedOutput {
        return unitOfWork.execute {
            if (patrimonyRepo.existsByName(input.title))
                throw Error("Patrimony name already exists")

            if (input.accountIds.isNotEmpty())
                if (this.accountRepo.getManyByIds(input.accountIds).size != input.accountIds.size)
                    throw Error("Not all accounts are found")

            val newPatrimony = Patrimony(
                title = input.title,
                amount = input.amount,
                accountIds = input.accountIds.toMutableSet(),
                type = input.type
            )

            patrimonyRepo.create(newPatrimony)

            if (newPatrimony.amount > 0) {
                val firstSnapShot = PatrimonySnapshot(
                    patrimonyId = newPatrimony.id,
                    currentBalanceObserved = newPatrimony.amount,
                    date = LocalDate.now(),
                    status = PatrimonySnapshotStatusType.COMPLETED
                )

                snapshotRepo.create(firstSnapShot)
            }

            CreatedOutput(newPatrimony.id)
        }
    }
}