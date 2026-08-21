package dev.auguste.agni_api.core.usecases.patrimonies.dto

import java.util.UUID

data class GetPatrimonyOutput(
    val id: UUID,
    val title: String,
    val accountIds: List<UUID>,
    val amount: Double,
    val currentBalance: Double,
    val pastBalance: Double,
    val type: String,
    val isTotalFund: Boolean = false
)
