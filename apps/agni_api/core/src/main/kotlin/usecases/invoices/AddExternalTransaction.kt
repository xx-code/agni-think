package usecases.invoices

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingExternalTransEventContent
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.exceptions.AlreadyExistException
import domain.entities.ExternalTransaction
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.AddExternalTransactionInput

class AddExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): IUseCase<AddExternalTransactionInput, CreatedOutput> {
    override fun execAsync(input: AddExternalTransactionInput): CreatedOutput {

        val condition = QueryExtendBuilder<ExternalTransaction>()
            .addCondition("transactionId", QueryComparator.Equal, input.transactionId)
        val externalTransactions = externalTransactionRepo.getAll(
            QueryFilter.queryAll(),
            condition
        )

        if (externalTransactions.items.isNotEmpty())
            throw AlreadyExistException.Entity("all_external_transaction")

        val newExternalTransaction = ExternalTransaction(
            accountId = input.accountId,
            transactionId = input.transactionId,
            amount = input.amount,
            dateTransaction = input.dateTransaction,
            merchantName = input.merchantName,
            categoryPrimary = input.categoryPrimary,
            categoryDetail = input.categoryDetail,
            isTreated = input.isTreated
        )

        externalTransactionRepo.create(newExternalTransaction)

        if (newExternalTransaction.isTreated)
            eventRegister.notify(EventType.CREATE_EXTERNAL_TRANSACTION,
                CreateEmbeddingExternalTransEventContent(newExternalTransaction)
            )

        return CreatedOutput(newExternalTransaction.id)
    }
}