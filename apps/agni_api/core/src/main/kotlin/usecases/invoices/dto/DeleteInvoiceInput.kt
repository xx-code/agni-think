package usecases.invoices.dto

import java.util.UUID

data class DeleteInvoiceInput(val invoiceId: UUID,
                              val checkSinkingFund: Boolean = true, val checkTransfer: Boolean = true)