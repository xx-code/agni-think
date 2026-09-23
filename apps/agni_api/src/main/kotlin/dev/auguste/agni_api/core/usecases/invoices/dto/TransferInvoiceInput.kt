package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime
import java.util.UUID

data class TransferInvoiceInput(
    val accountIdFrom: UUID,
    val accountIdTo: UUID,
    val date: LocalDateTime,
    val amount: Double,
    val moduleSourcesLinker: List<InvoiceModuleLinker> = emptyList()
)
