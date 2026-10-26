package adapters

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import com.plaid.client.model.RemovedTransaction
import com.plaid.client.model.Transaction
import com.plaid.client.model.TransactionsSyncRequest
import configs.PlaidAuth
import domain.entities.BankRegister
import domain.entities.ExternalTransaction
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class PlaidBankingHandler(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val plaid: PlaidAuth,
    private val uow: IUnitOfWork
): IBankingHandlerAdapter {

    final val LOOP_READ_MAX_TRY = 10000

    data class ReadTransactionModel(
        val hasMore: Boolean,
        val added: List<Transaction>,
        val modified: List<Transaction>,
        val removed: List<RemovedTransaction>,
        val nextCursor: String
    )

    data class LoopReadTransactionModel(
        val newCursor: String,
        val externalTransactions: List<ExternalTransaction>
    )

    private fun readTransaction(cursor: String, accessToken: String, count: Int=100): ReadTransactionModel {
        val request = TransactionsSyncRequest()
        request.accessToken = accessToken
        request.count = count
        request.cursor = cursor
        val response = plaid.getClient()
            .transactionsSync(request)
            .execute()

        if (response.isSuccessful) {
            val resBody = response.body()!!
            return ReadTransactionModel(
                hasMore = resBody.hasMore,
                resBody.added,
                resBody.modified,
                resBody.removed,
                resBody.nextCursor
            )
        }

        plaid.handlePlaidError(response)
    }

    private fun loopTransactions(userId: UUID, accessToken: String, initCursor: String = "", isInitRequest: Boolean = false, iterations: Int? = null): LoopReadTransactionModel {
        var cursor = initCursor
        val seenIds = mutableSetOf<String>()
        val transactions = mutableListOf<Transaction>()
        var hasMore = true
        var iterationCount = 0

        while(hasMore && iterationCount < LOOP_READ_MAX_TRY) {
            val res = readTransaction(cursor, accessToken)
            for(transaction in res.added) {
                if (transaction.pending == true)
                    continue

                val tid = transaction.transactionId
                if (seenIds.contains(tid))
                    continue

                seenIds.add(tid)
                transactions.add(transaction)
            }

            cursor = res.nextCursor
            hasMore = res.hasMore
            iterationCount++
        }

        val newExternalTransactions = mutableListOf<ExternalTransaction>()
        val timeNow = LocalDateTime.now().toLocalTime()
        for (transaction in transactions) {
            val extTrs = ExternalTransaction(
                userId = userId,
                transactionId = transaction.transactionId,
                accountId = transaction.accountId,
                amount = transaction.amount,
                dateTransaction = transaction.date.atTime(timeNow),
                merchantName = transaction.merchantName ?: "",
                categoryPrimary = transaction.personalFinanceCategory?.primary ?: "",
                categoryDetail = transaction.personalFinanceCategory?.detailed ?: "",
                isTreated = false
            )
            newExternalTransactions.add(extTrs)
        }

        return LoopReadTransactionModel(
            cursor,
            newExternalTransactions
        )
    }

    override suspend fun syncBankTransactions(bankRegisters: List<BankRegister>) {
        uow.execute {
            val newExternalTransactions = mutableListOf<ExternalTransaction>()
            for (bank in bankRegisters) {
                val res = loopTransactions(bank.userId, bank.accessCode, bank.cursor)
                bank.cursor = res.newCursor
                newExternalTransactions.addAll(res.externalTransactions)
                bankRegisterRepo.update(bank)
            }
            externalTransactionRepo.createMany(newExternalTransactions)
        }
    }
}