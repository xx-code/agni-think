package dev.auguste.mcp.controllers

import adapters.IAuthenticateAdapter
import adapters.repositories.IVectorRepository
import dev.auguste.mcp.controllers.models.CreateInvoiceModel
import dev.auguste.mcp.controllers.models.InvoiceSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

@Component
class InvoiceController(
    private val getAllInvoiceUseCase: IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>>,
    private val createInvoiceUseCase: IUseCase<CreateInvoiceInput, CreatedOutput>,
    private val vectorDb: IVectorRepository,
    private val auth: IAuthenticateAdapter,
    @Value("\${embedding.collection.invoice}") private val collectionName: String,
) {

    @McpTool(
        name = "getAllInvoices",
        description = "List invoices with their totals, status, date, account, money direction and transactions. Supports filtering by account, date range, status, type, freeze state, movement direction, category, tag, budget and amount range. Use this for questions about past spending or income.  For invoice avoid QueryAll = true If you want a lot of invoice use queryInvoiceDocuments tool",
        annotations = McpTool.McpAnnotations(
            title = "List invoices",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllInvoices(
        @McpToolParam(description = "Search options: pagination plus optional account, date, status, type, freeze, movement, category, tag, budget and amount filters.")
        input: InvoiceSearchModel,
    ): ListOutput<GetInvoiceOutput> {
        return runBlocking { getAllInvoiceUseCase.execute(input.toDomain()).getOrThrow() }
    }

    @McpTool(
        name = "createInvoice",
        description = "Create a new invoice (or add lines to an existing invoice). Use this when the user wants to record a purchase, income, bill or expense. The invoice needs an account, a type, a money direction and at least one line with amount, categoryId and description. Prefer asking the user for missing account or category UUIDs before calling. This will create a Pending Invoice, as LLM you cannot create other than pending invoice.",
        annotations = McpTool.McpAnnotations(
            title = "Create invoice",
            readOnlyHint = false,
            destructiveHint = false,
            idempotentHint = false,
            openWorldHint = false,
        ),
    )
    fun createInvoice(
        @McpToolParam(description = "Invoice to create: account, date, type, money direction, status and one or more transaction lines.")
        input: CreateInvoiceModel,
    ): CreatedOutput {
        return runBlocking {
            val currentUser = auth.getCurrentUser()
            createInvoiceUseCase.execute(input.toDomain(currentUser.id)).getOrThrow()
        }
    }

    @McpTool(
        name = "queryInvoiceDocuments",
        description = "Search stored invoice documents with a natural-language question. Use this for fuzzy questions about past vendors, receipt contents or invoice items when exact filters are unknown. The answer is a text excerpt of the most similar invoice documents.",
        annotations = McpTool.McpAnnotations(
            title = "Search invoice documents",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun queryInvoiceDocuments(
        @McpToolParam(description = "Natural-language question or keywords to search in invoice documents, for example \"groceries at Metro in October\".")
        question: String,
        @McpToolParam(description = "Maximum number of similar documents to return. Use 5 for a concise answer, up to 20 for a broader search. Default: 5.")
        limit: Int = 5,
    ): String {
        return vectorDb.query(question, collectionName, limit)
    }
}
