package usecases.invoices

import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingExternalTransEventContent
import adapters.repositories.IRepository
import domain.entities.ExternalTransaction
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.invoices.dto.TreatAnExternalTransactionInput

class TreatAnExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): IUseCase<TreatAnExternalTransactionInput, Unit> {
    override fun execAsync(input: TreatAnExternalTransactionInput) {
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