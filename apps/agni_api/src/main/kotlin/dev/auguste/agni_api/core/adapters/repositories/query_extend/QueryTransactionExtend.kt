package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Transaction
import java.util.UUID

class QueryTransactionExtend(
    val invoiceIds: Set<UUID>?,
    val categoryIds: Set<UUID>?,
    val tagIds: Set<UUID>?,
    val budgetIds: Set<UUID>?,
    val minAmount: Double?,
    val maxAmount: Double?
): IQueryExtend<Transaction> {

    override fun isStatisfy(entity: Transaction): Boolean {
        if (invoiceIds != null && !invoiceIds.contains(entity.invoiceId))
            return false

        if (categoryIds != null && !categoryIds.contains(entity.categoryId))
            return false

        if (tagIds != null && !tagIds.any { it in entity.tagIds })
            return false

        if (budgetIds != null && !budgetIds.any { it in entity.budgetIds })
            return false

        if (minAmount != null && minAmount < entity.amount)
            return false

        if (maxAmount != null && maxAmount > entity.amount)
            return false

        return true
    }
}