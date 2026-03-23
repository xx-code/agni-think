package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.ExternalTransaction

class QueryExternalTransactionExtend(
    val isTreated: Boolean?,
): IQueryExtend<ExternalTransaction> {
    override fun isStatisfy(entity: ExternalTransaction): Boolean {
        return !(isTreated != null && isTreated != entity.isTreated)
    }
}