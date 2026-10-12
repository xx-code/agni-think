package usecases.invoices

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingExternalTransEventContent
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.exceptions.AlreadyExistException
import domain.entities.Account
import domain.entities.BankRegister
import domain.entities.ExternalTransaction
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.invoices.dto.AddExternalTransactionInput

class AddExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val eventRegister: IEventRegister,
    auth: IAuthenticateAdapter
): UseCase<AddExternalTransactionInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: AddExternalTransactionInput): CreatedOutput {
        val currentUser = getCurrentUser()

        val condition = QueryExtendBuilder<ExternalTransaction>()
            .addCondition("transactionId", QueryComparator.Equal, input.transactionId)
        val externalTransactions = externalTransactionRepo.getAll(
            QueryFilter.queryAll(),
            condition
        )

        if (externalTransactions.items.isNotEmpty())
            throw AlreadyExistException.Entity("all_external_transaction")


        val newExternalTransaction = ExternalTransaction(
            userId = currentUser.id,
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