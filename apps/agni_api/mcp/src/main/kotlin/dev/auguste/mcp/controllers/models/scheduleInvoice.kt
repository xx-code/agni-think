package dev.auguste.mcp.controllers.models

import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.schedule_Invoices.dto.GetAllScheduleInvoiceInput

data class ScheduleInvoiceSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 scheduled invoices.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),
) {
    fun toDomain(): GetAllScheduleInvoiceInput {
        return GetAllScheduleInvoiceInput(queryFilter = page.toDomain())
    }
}
