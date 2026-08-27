package dev.auguste.agni_api.core.value_objects

import java.time.LocalDate
import java.util.UUID

data class ProvisionPayment(
    val accountId: UUID,
    val categoryId: UUID,
    val budgetIds: Set<UUID>,
    val tagIds: Set<UUID>,
    val paymentAmount: Double,
    val scheduler: Scheduler,
    val endDate: LocalDate
) : IValueObject {

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "account_id" to accountId.toString(),
            "category_id" to categoryId.toString(),
            "budget_ids" to budgetIds.map { it.toString() },
            "tag_ids" to tagIds.map { it.toString() },
            "payment_amount" to paymentAmount,
            "scheduler" to scheduler.toMap(),
            "end_date" to endDate.toString()
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): ProvisionPayment {
            val accountId = UUID.fromString(map["account_id"] as String)
            val categoryId = UUID.fromString(map["category_id"] as String)

            @Suppress("UNCHECKED_CAST")
            val budgetIds = (map["budget_ids"] as? List<String>)
                ?.map { UUID.fromString(it) }
                ?.toSet() ?: emptySet()

            @Suppress("UNCHECKED_CAST")
            val tagIds = (map["tag_ids"] as? List<String>)
                ?.map { UUID.fromString(it) }
                ?.toSet() ?: emptySet()

            var paymentAmount = map["payment_amount"]
            if (paymentAmount is Int) {
                paymentAmount = paymentAmount.toDouble()
            }

            @Suppress("UNCHECKED_CAST")
            val schedulerMap = map["scheduler"] as? Map<String, Any>
                ?: throw IllegalArgumentException("Scheduler map missing")
            val scheduler = Scheduler.fromMap(schedulerMap)

            val endDate = LocalDate.parse(map["end_date"] as String)

            return ProvisionPayment(
                accountId = accountId,
                categoryId = categoryId,
                budgetIds = budgetIds,
                tagIds = tagIds,
                paymentAmount = paymentAmount as Double,
                scheduler = scheduler,
                endDate = endDate
            )
        }
    }
}

