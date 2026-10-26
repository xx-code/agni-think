package usecases.invoices

import usecases.UseCase
import adapters.dto.VectorDocumentInput
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateManyEmbeddingExternalTransEventContent
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.events.listeners.ICreateManyExternalTransactionListener
import adapters.repositories.IRepository
import adapters.repositories.IVectorRepository
import domain.exceptions.ValidationException
import domain.entities.ExternalTransaction
import domain.entities.Notification
import usecases.dto.BackgroundTaskOut
import java.util.UUID

class CreateManyExternalTransactionEmbedding(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val vectorRepo: IVectorRepository,
    private val eventRegister: IEventRegister,
    private val collectionName: String
): UseCase<List<UUID>, BackgroundTaskOut>(), ICreateManyExternalTransactionListener{
    private var event: CreateManyEmbeddingExternalTransEventContent? = null

    override suspend fun process(input: List<UUID>): BackgroundTaskOut {
        try {
            val transactions = externalTransactionRepo.getManyByIds(input.toSet())
            if (transactions.size != input.size)
                throw ValidationException.SomeExternalTransactionsNotFound()

            val documents = transactions.map { trans ->
                VectorDocumentInput(
                    id = trans . id,
                    document = """
                        accountId=${trans.accountId};
                        amount=${trans.amount};
                        dateTransaction=${trans.dateTransaction};
                        merchantName=${trans.merchantName};
                        categoryPrimary=${trans.categoryPrimary};
                        categoryDetail=${trans.categoryDetail};
                    """.trimIndent()
                )
            }

            vectorRepo.addDocuments(collectionName, documents)

            return BackgroundTaskOut("External Transaction [${transactions.map { it.id }}] added")
        } catch(err: Exception) {
            eventRegister.notify(
                EventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Fail to create multiple  external embdedding transaction",
                    message = "External was failed to create embedding invoice: ${err.message}",
                    type = NotificationType.Error,
                    userId = Notification.SYSTEM_USER_ID,
                )
            )
            return BackgroundTaskOut("Error while creating External Transaction embedding. ${err.localizedMessage}")
        }
    }

    override fun server(content: CreateManyEmbeddingExternalTransEventContent) {
        event = content
    }

    override suspend fun update() {
        event?.let {
            process(it.transactions.map { trans -> trans.id })
        }
        event = null
    }

}