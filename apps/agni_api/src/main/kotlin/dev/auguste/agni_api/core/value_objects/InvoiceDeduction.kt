package dev.auguste.agni_api.core.value_objects

import java.util.UUID

data class InvoiceDeduction(val deductionId: UUID, val amount: Double)
