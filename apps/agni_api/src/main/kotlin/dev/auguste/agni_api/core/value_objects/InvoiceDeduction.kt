package dev.auguste.agni_api.core.value_objects

import java.util.UUID

data class InvoiceDeduction(val deductionId: UUID, val amount: Double) {
    fun toMap(): Map<String, Any> {
        return mapOf("deduction_id" to deductionId, "amount" to amount)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): InvoiceDeduction {
            if (map == null)
                return InvoiceDeduction(UUID.randomUUID(), 0.0)

            if (!map.containsKey("deduction_id") || !map.containsKey("amount"))
                return InvoiceDeduction(UUID.randomUUID(), 0.0)


            var amount = map["amount"]
            if (amount is Int)
                amount = amount.toDouble()

            return InvoiceDeduction(UUID.fromString(map.getValue("deduction_id") as String) , amount as Double)
        }
    }
}
