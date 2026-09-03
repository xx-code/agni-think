package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceType

class QueryScheduleInvoiceExtend(
    val comparatorDueDate: QueryDateComparator? = null,
    val comparatorEndDate: QueryDateComparator? = null,
    val type: InvoiceType? = null,

    ): IQueryExtend<ScheduleInvoice> {

    override fun isStatisfy(entity: ScheduleInvoice): Boolean {
        if (comparatorDueDate != null) {
            val resComp = when(comparatorDueDate.comparator) {
                QueryComparator.Greater ->  comparatorDueDate.date > entity.scheduler.date
                QueryComparator.GreaterOrEquals -> comparatorDueDate.date >= entity.scheduler.date
                QueryComparator.Lesser -> comparatorDueDate.date < entity.scheduler.date
                QueryComparator.LesserOrEquals ->  comparatorDueDate.date <= entity.scheduler.date
                QueryComparator.Equal ->  comparatorDueDate.date == entity.scheduler.date
                else -> {false}
            }

            if (!resComp)
                return false
        }

        if (comparatorEndDate != null && entity.endDate != null) {
            val resComp = when(comparatorEndDate.comparator) {
                QueryComparator.Greater ->  comparatorEndDate.date > entity.endDate
                QueryComparator.GreaterOrEquals -> comparatorEndDate.date >= entity.endDate
                QueryComparator.Lesser -> comparatorEndDate.date < entity.endDate
                QueryComparator.LesserOrEquals ->  comparatorEndDate.date <= entity.endDate
                QueryComparator.Equal ->  comparatorEndDate.date == entity.endDate
                else -> {false}
            }

            if (!resComp)
                return false
        }

        if (type != null && type != entity.type) {
            return false
        }

        return true
    }
}