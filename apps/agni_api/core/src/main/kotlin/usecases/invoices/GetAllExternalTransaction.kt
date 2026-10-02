package usecases.invoices

import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryExternalTransactionExtend
import domain.entities.ExternalTransaction
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetAllExternalTransactionInput
import usecases.invoices.dto.GetExternalTransactionOutput

class GetAllExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
): IUseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>> {
    override fun execAsync(input: GetAllExternalTransactionInput): ListOutput<GetExternalTransactionOutput> {
        val externalTransactions = externalTransactionRepo.getAll(input.query, queryExtend = QueryExternalTransactionExtend(isTreated = input.isTreated))

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