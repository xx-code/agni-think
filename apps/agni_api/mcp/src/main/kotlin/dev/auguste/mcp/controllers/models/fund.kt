package dev.auguste.mcp.controllers.models

import domain.enums.FundType
import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.funds.dto.GetAllFundInput

data class FundSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 funds.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Optional fund category filter. Accepted values: \"Emergency\", \"Amortization\", \"SinkingFund\", \"ProjectTarget\", \"Opportunity\", \"SavingsGeneral\" (case-insensitive). Omit to include all fund types.",
        required = false,
    )
    val type: String? = null,
) {
    fun toDomain(): GetAllFundInput {
        return GetAllFundInput(
            queryFilter = page.toDomain(),
            type = type?.let { FundType.fromString(it) },
        )
    }
}
