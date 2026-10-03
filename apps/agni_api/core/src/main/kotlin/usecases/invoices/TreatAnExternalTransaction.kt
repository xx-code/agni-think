package usecases.invoices

import usecases.UseCase
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingExternalTransEventContent
import adapters.repositories.IRepository
import domain.entities.ExternalTransaction
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.invoices.dto.TreatAnExternalTransactionInput

class TreatAnExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): UseCase<TreatAnExternalTransactionInput, Unit>() {
    override suspend fun process(input: TreatAnExternalTransactionInput) {
        val externalTransaction = externalTransactionRepo.get(input.transactionId) ?: throw NotFoundException.SingleEntity(input.transactionId, "external_transaction")
        if (externalTransaction.isTreated)
            throw ValidationException.TreatedTransactionAlreadyTreated()

        externalTransaction.isTreated = true
        externalTransactionRepo.update(externalTransaction)
        eventRegister.notify(EventType.CREATE_EXTERNAL_TRANSACTION,
            CreateEmbeddingExternalTransEventContent(externalTransaction)
        )
    }
}