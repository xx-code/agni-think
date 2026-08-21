package dev.auguste.agni_api.core.usecases.patrimonies.dto

import java.util.UUID

data class GetPatrimonyInput(
    val id: UUID,
    val isTotalFund: Boolean = false
)