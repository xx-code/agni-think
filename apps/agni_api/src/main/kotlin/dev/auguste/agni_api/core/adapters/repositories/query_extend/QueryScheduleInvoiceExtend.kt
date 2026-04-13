package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import java.util.Date

class QueryScheduleInvoiceExtend(
    val comparatorDueDate: QueryDateComparator? = null,
    val type: InvoiceType? = null,

    ): IQueryExtend<ScheduleInvoice> {

    override fun isStatisfy(entity: ScheduleInvoice): Boolean {
        if (comparatorDueDate != null) {
            val resComp = when(comparatorDueDate.comparator) {
                ComparatorType.Greater ->  comparatorDueDate.date > entity.scheduler.date
                ComparatorType.GreaterOrEquals -> comparatorDueDate.date >= entity.scheduler.date
                ComparatorType.Lesser -> comparatorDueDate.date < entity.scheduler.date
                ComparatorType.LesserOrEquals ->  comparatorDueDate.date <= entity.scheduler.date
                ComparatorType.Equal ->  comparatorDueDate.date == entity.scheduler.date
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