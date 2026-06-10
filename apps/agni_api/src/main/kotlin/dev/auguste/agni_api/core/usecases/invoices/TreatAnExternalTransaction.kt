package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingExternalTransEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.invoices.dto.TreatAnExternalTransactionInput

class TreatAnExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): IUseCase<TreatAnExternalTransactionInput, Unit> {
    override fun execAsync(input: TreatAnExternalTransactionInput) {
        val externalTransaction = externalTransactionRepo.get(input.transactionId) ?: throw DomainException.NotFound.ExternalTransaction(input.transactionId.toString())
        if (externalTransaction.isTreated)
            throw DomainException.BusinessLogic.TreatedTransactionAlreadyTreated()

        externalTransaction.isTreated = true
        externalTransactionRepo.update(externalTransaction)
        eventRegister.notify(EventType.CREATE_EXTERNAL_TRANSACTION,
            CreateEmbeddingExternalTransEventContent(externalTransaction)
        )
    }
}