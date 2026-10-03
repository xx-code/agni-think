package usecases.invoices

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateManyEmbeddingExternalTransEventContent
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.ExternalTransaction
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import domain.exceptions.ValidationException
import usecases.invoices.dto.AddExternalTransactionInput

class AddManyExternalTransactions(
    private val externalTransRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister
): IUseCase<List<AddExternalTransactionInput>, List<CreatedOutput>> {
    override fun execAsync(input: List<AddExternalTransactionInput>): List<CreatedOutput>  {
        var newExternalTransactions = input.map {
            ExternalTransaction(
                accountId = it.accountId,
                transactionId = it.transactionId,
                amount = it.amount,
                dateTransaction = it.dateTransaction,
                merchantName = it.merchantName,
                categoryPrimary = it.categoryPrimary,
                categoryDetail = it.categoryDetail,
                isTreated = it.isTreated
            )
        }

        val condition = QueryExtendBuilder<ExternalTransaction>()
            .addCondition("transactionId", QueryComparator.In, input.map { it.transactionId }.toSet())
        val externalTransactions = externalTransRepo.getAll(
            query = QueryFilter(queryAll = true),
            condition
        )

        newExternalTransactions = newExternalTransactions.filter { trans -> trans.transactionId !in externalTransactions.items.map { it.transactionId } }

        if (newExternalTransactions.isEmpty())
            throw ValidationException.AllNewTransactionsAlreadyAdded()

        externalTransRepo.createMany(newExternalTransactions)

        eventRegister.notify(EventType.CREATE_MANY_EXTERNAL_TRANSACTION,
            CreateManyEmbeddingExternalTransEventContent(newExternalTransactions.filter { it.isTreated }))

        return newExternalTransactions.map { CreatedOutput(it.id) }.toList()
    }
}