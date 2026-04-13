package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.ExternalTransaction

class QueryExternalTransactionExtend(
    val transactionIds: Set<String>? = null,
    val isTreated: Boolean? = null,
): IQueryExtend<ExternalTransaction> {
    override fun isStatisfy(entity: ExternalTransaction): Boolean {
        if (isTreated != null && isTreated != entity.isTreated)
            return false

        if (transactionIds != null && !transactionIds.contains(entity.transactionId))
            return false

        return true
    }
}