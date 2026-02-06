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
    val patrimonyRepo: IRepository<Patrimony>,
    val accountRepo: IRepository<Account>,
    val snapshotRepo: IRepository<PatrimonySnapshot>,
    val unitOfWork: IUnitOfWork): IUseCase<CreatePatrimonyInput, CreatedOutput> {

    override fun execAsync(input: CreatePatrimonyInput): CreatedOutput {
        try {
            unitOfWork.start()

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

            unitOfWork.commit()

            return CreatedOutput(newPatrimony.id)
        } catch (error: Throwable) {
            unitOfWork.rollback()
            throw error
        }
    }
}