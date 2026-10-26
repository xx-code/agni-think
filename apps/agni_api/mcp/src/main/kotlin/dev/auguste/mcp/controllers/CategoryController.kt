package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.CategorySearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.categories.dto.GetAllCategoryInput
import usecases.categories.dto.GetCategoryOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import java.util.UUID

@Component
class CategoryController(
    private val getAllCategoryUseCase: IUseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>>,
) {
    @McpTool(
        name = "getAllCategories",
        description = "List spending and income categories with their UUID, title, icon, color and archive state. Supports filtering system categories and archived categories. Use this to map category names to UUIDs before filtering invoices or analyses.",
        annotations = McpTool.McpAnnotations(
            title = "List categories",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllCategories(
        @McpToolParam(description = "Search options: pagination, origin filter (system or user) and archive filter.")
        input: CategorySearchModel,
    ): ListOutput<GetCategoryOutput> {
        return runBlocking { getAllCategoryUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
