package usecases.invoices.dto

import java.util.UUID

data class TreatAnExternalTransactionInput(
    val transactionId: UUID,
)
