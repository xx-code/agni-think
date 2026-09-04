package dev.auguste.agni_api.core.usecases.tags.dto

import java.util.UUID

data class UpdateTagInput(
    val id: UUID,
    val value: String? = null,
    val color: String? = null,
    val archive: Boolean? = null
)
