package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.EmbeddingDocument
import dev.auguste.agni_api.core.adapters.IEmbeddingService
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateManyEmbeddingExternalTransEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.events.listeners.ICreateManyExternalTransactionListener
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class CreateManyExternalTransactionEmbedding(
    private val externalTransactionRepo: IRepository<ExternalTransaction>,
    private val embeddingService: IEmbeddingService,
    private val eventRegister: IEventRegister,
    private val collectionName: String
) : IUseCase<List<UUID>, BackgroundTaskOut>, ICreateManyExternalTransactionListener{
    private var event: CreateManyEmbeddingExternalTransEventContent? = null

    override fun execAsync(input: List<UUID>): BackgroundTaskOut {
        try {
            val transactions = externalTransactionRepo.getManyByIds(input.toSet())
            if (transactions.size != input.size)
                throw Error("Some External transactions not found")

            val documents = transactions.map { trans ->
                EmbeddingDocument(
                    id = trans.id,
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

            embeddingService.addEmbeddingDocument(collectionName, documents)

            return BackgroundTaskOut("External Transaction [${transactions.map { it.id }}] added")
        } catch(err: Exception) {
            eventRegister.notify(
                EventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Fail to create multiple  external embdedding transaction",
                    message = "External was failed to create embedding invoice: ${err.message}",
                    type = NotificationType.Error
                )
            )
            return BackgroundTaskOut("Error while creating External Transaction embedding. ${err.localizedMessage}")
        }
    }

    override fun server(content: CreateManyEmbeddingExternalTransEventContent) {
        event = content
    }

    override fun update() {
        event?.let {
            execAsync(it.transactions.map { trans -> trans.id })
        }
        event = null
    }

}