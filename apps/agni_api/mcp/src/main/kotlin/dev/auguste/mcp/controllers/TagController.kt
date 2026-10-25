package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.TagSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.tags.dto.GetAllTagInput
import usecases.tags.dto.GetTagOutput
import java.util.UUID

@Component
class TagController(
    private val getAllTagUseCase: IUseCase<GetAllTagInput, ListOutput<GetTagOutput>>,
) {
    @McpTool(
        name = "getAllTags",
        description = "List tags with their UUID, value, color and archive state. Supports filtering system tags and archived tags. Use this to map tag names to UUIDs before filtering invoices.",
        annotations = McpTool.McpAnnotations(
            title = "List tags",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllTags(
        @McpToolParam(description = "Search options: pagination, origin filter (system or user) and archive filter.")
        input: TagSearchModel,
    ): ListOutput<GetTagOutput> {
        return runBlocking { getAllTagUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
