package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.IEmbeddingService
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingInvoiceEventContent
import dev.auguste.agni_api.core.adapters.events.IEventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.events.listeners.ICreateEmbeddingInvoiceEventListener
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class CreateInvoiceEmbedding(
    private val eventRegister: IEventRegister,
    private val categoryRepo: IRepository<Category>,
    private val budgetRepo: IRepository<Budget>,
    private val tagsRepo: IRepository<Tag>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val embeddingService: IEmbeddingService
): IUseCase<UUID, BackgroundTaskOut>, ICreateEmbeddingInvoiceEventListener {
    private var event: CreateEmbeddingInvoiceEventContent? = null

    fun getCat(id: UUID?, categories: List<Category>): String {
        return categories.find { it.id == id }?.title ?: "?"
    }

    fun getTag(id: UUID?, tags: List<Tag>): String {
        return tags.find { it.id == id }?.value ?: "?"
    }

    fun getBudget(id: UUID?, budgets: List<Budget>): String {
        return budgets.find { it.id == id }?.title ?: "?"
    }

    override fun execAsync(input: UUID): BackgroundTaskOut {
        try {
            val invoice = getInvoice.execAsync(input)

            val categories = categoryRepo.getManyByIds(invoice.transactions.map { it.categoryId }.toSet())
            val tags = tagsRepo.getManyByIds(invoice.transactions.flatMap { it.tagIds }.toSet())
            val budgets = budgetRepo.getManyByIds(invoice.transactions.flatMap { it.budgetIds }.toSet())

            if (invoice.transactions.isEmpty())
                throw Error("Transactions must not be empty")

            val transactionStr = invoice.transactions.joinToString("\n") { trans ->
                val tagStr = trans.tagIds.joinToString(", ") { getTag(it, tags) } ?: "none"
                val budgetStr = trans.budgetIds.joinToString(", ") { getBudget(it, budgets) } ?: "none"

                "category: ${getCat(trans.categoryId, categories)} - ${trans.description} " +
                        "with tags: [$tagStr] " +
                        "with budgets: [$budgetStr] " +
                        "amount: ${"%.2f".format(trans.amount)}"
            }

            val document = "Invoice (type: ${invoice.type}) dated ${invoice.date} - " +
                    "Total: ${"%.2f".format(invoice.total)}, Subtotal: ${"%.2f".format(invoice.subTotal)}. " +
                    "Lines: \n$transactionStr"

            embeddingService.addEmbeddingDocument(invoice.id, document)

            eventRegister.notify(
                IEventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Transaction Embedding Created",
                    message = "Invoice was created: ${invoice.id}",
                    type = NotificationType.Success
                )
            )

            return BackgroundTaskOut("Embedding invoice created")
        } catch (err: Exception) {
            eventRegister.notify(
                IEventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Fail to create embedding invoice",
                    message = "Invoice was failed to create embedding invoice: ${err.message}",
                    type = NotificationType.Error
                )
            )

            return BackgroundTaskOut("Error while creating an invoice embedding. ${err.localizedMessage}")
        }
    }

    override fun update() {
        event?.let {
           execAsync(it.invoice.id)
        }
        event = null
    }

    override fun serve(content: CreateEmbeddingInvoiceEventContent) {
        event = content
    }
}