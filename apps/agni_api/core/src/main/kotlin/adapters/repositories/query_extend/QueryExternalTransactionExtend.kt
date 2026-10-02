package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.ExternalTransaction

class QueryExternalTransactionExtend(
    val transactionIds: Set<String>? = null,
    val isTreated: Boolean? = null,
): IQueryExtend<domain.entities.ExternalTransaction> {
    override fun isStatisfy(entity: domain.entities.ExternalTransaction): Boolean {
        if (isTreated != null && isTreated != entity.isTreated)
            return false

        if (transactionIds != null && !transactionIds.contains(entity.transactionId))
            return false

        return true
    }
}