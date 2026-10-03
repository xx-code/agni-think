package usecases.invoices

import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.ExternalTransaction
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetAllExternalTransactionInput
import usecases.invoices.dto.GetExternalTransactionOutput

class GetAllExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
): IUseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>> {
    override fun execAsync(input: GetAllExternalTransactionInput): ListOutput<GetExternalTransactionOutput> {
        val conditionExternalTransaction = QueryExtendBuilder<ExternalTransaction>()
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