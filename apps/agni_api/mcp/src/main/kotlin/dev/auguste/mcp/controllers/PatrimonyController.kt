package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.PatrimonyGetModel
import dev.auguste.mcp.controllers.models.PatrimonySearchModel
import dev.auguste.mcp.controllers.models.PatrimonySnapshotSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.patrimonies.dto.GetAllPatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyOutput
import usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import java.util.UUID

@Component
class PatrimonyController(
    private val getAllPatrimoniesUseCase: IUseCase<GetAllPatrimonyInput, ListOutput<GetPatrimonyOutput>>,
) {
    @McpTool(
        name = "getAllPatrimonies",

        annotations = McpTool.McpAnnotations(
            title = "List patrimonies",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllPatrimonies(
        @McpToolParam(description = "Pagination and sorting options. Omit or send an empty object to get the first 15 patrimonies.")
        input: PatrimonySearchModel,
    ): ListOutput<GetPatrimonyOutput> {
        return runBlocking { getAllPatrimoniesUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
