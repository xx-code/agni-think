package usecases.invoices

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.ExternalTransaction
import usecases.dto.ListOutput
import usecases.invoices.dto.GetAllExternalTransactionInput
import usecases.invoices.dto.GetExternalTransactionOutput

class GetAllExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    auth: IAuthenticateAdapter
): UseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>>(auth = auth) {
    override suspend fun process(input: GetAllExternalTransactionInput): ListOutput<GetExternalTransactionOutput> {
        val currentUser = getCurrentUser()
        val conditionExternalTransaction = QueryExtendBuilder<ExternalTransaction>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
            .addCondition("isTreated", QueryComparator.Equal, input.isTreated)
        val externalTransactions = externalTransactionRepo.getAll(input.query, conditionExternalTransaction)

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