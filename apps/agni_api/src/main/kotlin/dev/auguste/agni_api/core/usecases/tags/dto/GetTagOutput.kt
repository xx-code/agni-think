package dev.auguste.agni_api.core.usecases.tags.dto

import java.util.UUID

data class GetTagOutput(
    val id: UUID,
    val value: String,
    val color: String?,
    val isSystem: Boolean
)
