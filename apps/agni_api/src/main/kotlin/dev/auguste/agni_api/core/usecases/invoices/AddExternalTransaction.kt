package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingExternalTransEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.AddExternalTransactionInput

class AddExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): IUseCase<AddExternalTransactionInput, CreatedOutput> {
    override fun execAsync(input: AddExternalTransactionInput): CreatedOutput {
        val newExternalTransaction = ExternalTransaction(
            accountId = input.accountId,
            amount = input.amount,
            dateTransaction = input.dateTransaction,
            merchantName = input.merchantName,
            categoryPrimary = input.categoryPrimary,
            categoryDetail = input.categoryDetail,
            isTreated = input.isTreated
        )

        externalTransactionRepo.create(newExternalTransaction)

        if (newExternalTransaction.isTreated)
            eventRegister.notify(EventType.CREATE_EMBEDDING_EXTERNAL_TRANSACTION,
                CreateEmbeddingExternalTransEventContent(newExternalTransaction)
            )

        return CreatedOutput(newExternalTransaction.id)
    }
}