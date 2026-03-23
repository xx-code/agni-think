package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingExternalTransEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.TreatAnExternalTransactionInput

class TreatAnExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): IUseCase<TreatAnExternalTransactionInput, Unit> {
    override fun execAsync(input: TreatAnExternalTransactionInput) {
        val externalTransaction = externalTransactionRepo.get(input.transactionId) ?: throw Error("External transaction not found")
        if (externalTransaction.isTreated)
            throw Error("Treated transaction already treated")

        externalTransaction.isTreated = true
        externalTransactionRepo.update(externalTransaction)
        eventRegister.notify(EventType.CREATE_EMBEDDING_EXTERNAL_TRANSACTION,
            CreateEmbeddingExternalTransEventContent(externalTransaction)
        )
    }
}