package usecases.patrimonies

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.enums.PatrimonySnapshotStatusType
import usecases.dto.CreatedOutput
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import usecases.UseCase
import usecases.patrimonies.dto.CreatePatrimonyInput
import java.time.LocalDate

class CreatePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>,
    private val snapshotRepo: IRepository<PatrimonySnapshot>,
    unitOfWork: IUnitOfWork): UseCase<CreatePatrimonyInput, CreatedOutput>(unitOfWork) {

    override suspend fun process(input: CreatePatrimonyInput): CreatedOutput {
        if (patrimonyRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "patrimony")

        if (input.accountIds.isNotEmpty())
            if (this.accountRepo.getManyByIds(input.accountIds).size != input.accountIds.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.accountIds.joinToString()), "account")

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

        return CreatedOutput(newPatrimony.id)
    }
}