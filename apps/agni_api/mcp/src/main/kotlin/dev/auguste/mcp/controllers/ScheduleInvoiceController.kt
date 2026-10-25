package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.ScheduleInvoiceSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.GetAllScheduleInvoiceInput
import usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import java.util.UUID

@Component
class ScheduleInvoiceController(
    private val getAllScheduleInvoiceUseCase: IUseCase<GetAllScheduleInvoiceInput, ListOutput<GetScheduleInvoiceOutput>>,
) {
    @McpTool(
        name = "getAllScheduleInvoices",
        description = "List scheduled (recurring) invoices with their amount, account, category, due date, recurrence, pause state and end date. Use this to review recurring bills and expected income.",
        annotations = McpTool.McpAnnotations(
            title = "List scheduled invoices",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllScheduleInvoices(
        @McpToolParam(description = "Pagination and sorting options. Omit or send an empty object to get the first 15 scheduled invoices.")
        input: ScheduleInvoiceSearchModel,
    ): ListOutput<GetScheduleInvoiceOutput> {
        return runBlocking { getAllScheduleInvoiceUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
