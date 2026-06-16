package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.InternalLoan
import java.util.UUID

class QueryInternalLoanExtend(
    val invoiceId: UUID? = null,
    val fundSourceId: UUID? = null,
    val creditCardId: UUID? = null,
    val refundFreezeId: UUID? = null,
    val scheduleDueDateComparator: QueryDateComparator? = null
): IQueryExtend<InternalLoan> {
    override fun isStatisfy(entity: InternalLoan): Boolean {
        if (invoiceId != null && entity.invoiceId != invoiceId)
            return false
        if (fundSourceId != null && entity.fundSourceId != fundSourceId)
            return false

        if (creditCardId != null && entity.creditTargetId != creditCardId)
            return false

        if (refundFreezeId != null && entity.trackRefunds.contains(refundFreezeId))
            return false

        if (scheduleDueDateComparator != null) {
            val isValid = when(scheduleDueDateComparator.comparator) {
                ComparatorType.Greater ->  scheduleDueDateComparator.date > entity.dueDate.atStartOfDay()
                ComparatorType.GreaterOrEquals -> scheduleDueDateComparator.date >= entity.dueDate.atStartOfDay()
                ComparatorType.Lesser -> scheduleDueDateComparator.date < entity.dueDate.atStartOfDay()
                ComparatorType.LesserOrEquals ->  scheduleDueDateComparator.date <= entity.dueDate.atStartOfDay()
                ComparatorType.Equal ->  scheduleDueDateComparator.date == entity.dueDate.atStartOfDay()
            }

            if (!isValid)
                return false
        }

        return true
    }
}