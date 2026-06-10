package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.EmbeddingDocument
import dev.auguste.agni_api.core.adapters.IEmbeddingService
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingExternalTransEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.events.listeners.ICreateExternalTransactionListener
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class CreateExternalTransaction(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val embeddingService: IEmbeddingService,
    private val eventRegister: IEventRegister,
    private val collectionExternalTransactionName: String,
) : IUseCase<UUID, BackgroundTaskOut>, ICreateExternalTransactionListener  {
    private var event: CreateEmbeddingExternalTransEventContent? = null

    override fun execAsync(input: UUID): BackgroundTaskOut {
        try {
            val trans = externalTransactionRepo.get(input) ?: throw dev.auguste.agni_api.core.entities.DomainException.BusinessLogic.Validation("External transaction not found")

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

    override fun update() {
        event?.let {
            execAsync(it.externalTransactions.id)
        }
        event = null
    }
}