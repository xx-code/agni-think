package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateManyEmbeddingExternalTransEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryExternalTransactionExtend
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.invoices.dto.AddExternalTransactionInput

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

        val externalTransactions = externalTransRepo.getAll(
            query = QueryFilter(queryAll = true),
            queryExtend = QueryExternalTransactionExtend(transactionIds = input.map { it.transactionId }.toSet()),
        )

        newExternalTransactions = newExternalTransactions.filter { trans -> trans.transactionId !in externalTransactions.items.map { it.transactionId } }

        if (newExternalTransactions.isEmpty())
            throw DomainException.BusinessLogic.AllNewTransactionsAlreadyAdded()

        externalTransRepo.createMany(newExternalTransactions)

        eventRegister.notify(EventType.CREATE_MANY_EXTERNAL_TRANSACTION,
            CreateManyEmbeddingExternalTransEventContent(newExternalTransactions.filter { it.isTreated }))

        return newExternalTransactions.map { CreatedOutput(it.id) }.toList()
    }
}