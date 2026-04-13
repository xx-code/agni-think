package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.InternalLoan
import java.util.UUID

class QueryInternalLoanExtend(
    val invoiceId: UUID? = null,
    val fundSourceId: UUID? = null,
    val creditCardId: UUID? = null
): IQueryExtend<InternalLoan> {
    override fun isStatisfy(entity: InternalLoan): Boolean {
        if (invoiceId != null && entity.invoiceId != invoiceId)
            return false
        if (fundSourceId != null && entity.fundSourceId != fundSourceId)
            return false

        if (creditCardId != null && entity.creditTargetId != creditCardId)
            return false

        return true
    }
}