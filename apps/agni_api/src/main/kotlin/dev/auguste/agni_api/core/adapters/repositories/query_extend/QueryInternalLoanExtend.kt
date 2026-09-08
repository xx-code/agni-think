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
                QueryComparator.Greater ->  scheduleDueDateComparator.date > entity.dueDate.atStartOfDay()
                QueryComparator.GreaterOrEquals -> scheduleDueDateComparator.date >= entity.dueDate.atStartOfDay()
                QueryComparator.Lesser -> scheduleDueDateComparator.date < entity.dueDate.atStartOfDay()
                QueryComparator.LesserOrEquals ->  scheduleDueDateComparator.date <= entity.dueDate.atStartOfDay()
                QueryComparator.Equal ->  scheduleDueDateComparator.date == entity.dueDate.atStartOfDay()
                else -> {false}
            }

            if (!isValid)
                return false
        }

        return true
    }
}