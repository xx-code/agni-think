package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.Transaction
import java.util.UUID

class QueryTransactionExtend(
    val invoiceIds: Set<UUID>?,
    val categoryIds: Set<UUID>?,
    val tagIds: Set<UUID>?,
    val budgetIds: Set<UUID>?,
    val minAmount: Double?,
    val maxAmount: Double?
): IQueryExtend<domain.entities.Transaction> {

    override fun isStatisfy(entity: domain.entities.Transaction): Boolean {
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