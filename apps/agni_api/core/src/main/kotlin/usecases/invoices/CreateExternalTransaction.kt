package usecases.invoices

import usecases.UseCase
import adapters.EmbeddingDocument
import adapters.IEmbeddingService
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingExternalTransEventContent
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.events.listeners.ICreateExternalTransactionListener
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.ExternalTransaction
import usecases.dto.BackgroundTaskOut
import java.util.UUID

class CreateExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val embeddingService: IEmbeddingService,
    private val eventRegister: IEventRegister,
    private val collectionExternalTransactionName: String,
): UseCase<UUID, BackgroundTaskOut>(), ICreateExternalTransactionListener  {
    private var event: CreateEmbeddingExternalTransEventContent? = null

    override suspend fun process(input: UUID): BackgroundTaskOut {
        try {
            val trans = externalTransactionRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "transaction")

            val docTrans = """
                accountId=${trans.accountId};
                amount=${trans.amount};
                dateTransaction=${trans.dateTransaction};
                merchantName=${trans.merchantName};
                categoryPrimary=${trans.categoryPrimary};
                categoryDetail=${trans.categoryDetail};
            """.trimIndent()

            embeddingService.addEmbeddingDocument(collectionExternalTransactionName,listOf(EmbeddingDocument(trans.id, docTrans)))

            return BackgroundTaskOut("External Transaction ${trans.id} added")
        } catch(err: Exception) {
            eventRegister.notify(
                EventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Fail to create embedding external transaction",
                    message = "External was failed to create embedding invoice: ${err.message}",
                    type = NotificationType.Error
                )
            )
            return BackgroundTaskOut("Error while creating External Transaction embedding. ${err.localizedMessage}")
        }
    }

    override fun server(content: CreateEmbeddingExternalTransEventContent) {
        event = content
    }

    override suspend fun update() {
        event?.let {
            process(it.externalTransactions.id)
        }
        event = null
    }
}