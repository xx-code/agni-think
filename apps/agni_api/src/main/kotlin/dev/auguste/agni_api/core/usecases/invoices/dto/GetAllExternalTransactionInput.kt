package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter

data class GetAllExternalTransactionInput(
    val query: QueryFilter,
    val isTreated: Boolean? = null,
)