package dev.auguste.mcp.controllers.models

import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.patrimonies.dto.GetAllPatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyInput
import usecases.patrimonies.dto.SourcePatrimonyType
import usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import java.util.UUID

data class PatrimonySearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 patrimonies.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),
) {
    fun toDomain(): GetAllPatrimonyInput {
        return GetAllPatrimonyInput(queryFilter = page.toDomain())
    }
}

data class PatrimonyGetModel(
    @field:McpToolParam(
        description = "Origin of the item to read. Accepted values: \"Patrimony\" (regular assets and liabilities), \"Fund\" (saving funds) or \"Provision\" (provisions). Default: \"Patrimony\".",
        required = false,
    )
    val sourceType: String = SourcePatrimonyType.PATRIMONY.value,

    @field:McpToolParam(
        description = "Set true to read the asset side, false for the liability side. Default: false.",
        required = false,
    )
    val isAsset: Boolean = false,
) {
    fun toDomain(patrimonyId: UUID): GetPatrimonyInput {
        return GetPatrimonyInput(
            id = patrimonyId,
            sourceType = SourcePatrimonyType.fromString(sourceType),
            isAsset = isAsset,
        )
    }
}

data class PatrimonySnapshotSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 snapshots.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Origin of the patrimony whose snapshots are requested. Accepted values: \"Patrimony\", \"Fund\", \"Provision\". Default: \"Patrimony\".",
        required = false,
    )
    val sourceType: String = SourcePatrimonyType.PATRIMONY.value,

    @field:McpToolParam(
        description = "Set true to read snapshots of an asset, false for a liability. Default: false.",
        required = false,
    )
    val isAsset: Boolean = false,
) {
    fun toDomain(patrimonyId: UUID): GetAllSnapshotPatrimonyInput {
        return GetAllSnapshotPatrimonyInput(
            patrimonyId = patrimonyId,
            query = page.toDomain(),
            sourcePatrimonyType = SourcePatrimonyType.fromString(sourceType),
            isAsset = isAsset,
        )
    }
}
