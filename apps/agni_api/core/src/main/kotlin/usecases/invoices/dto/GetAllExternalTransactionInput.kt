package usecases.invoices.dto

import adapters.dto.QueryFilter

data class GetAllExternalTransactionInput(
    val query: QueryFilter,
    val isTreated: Boolean? = null,
)