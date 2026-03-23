package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryExternalTransactionExtend
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllExternalTransactionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetExternalTransactionOutput

class GetAllExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
): IUseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>> {
    override fun execAsync(input: GetAllExternalTransactionInput): ListOutput<GetExternalTransactionOutput> {
        val externalTransactions = externalTransactionRepo.getAll(input.query, queryExtend = QueryExternalTransactionExtend(input.isTreated))

        return ListOutput(
            externalTransactions.items.map {
                GetExternalTransactionOutput(
                    id = it.id,
                    accountId = it.accountId,
                    amount = it.amount,
                    dateTransaction = it.dateTransaction,
                    merchantName = it.merchantName,
                    categoryPrimary = it.categoryPrimary,
                    categoryDetail = it.categoryDetail,
                    isTreated = it.isTreated
                )
            },
            total = externalTransactions.total
        )
    }
}