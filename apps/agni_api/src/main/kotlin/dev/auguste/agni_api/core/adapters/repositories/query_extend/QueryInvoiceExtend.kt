package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

class QueryInvoiceExtend(
    val accountIds: Set<UUID>?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val types: Set<InvoiceType>?,
    val status: InvoiceStatusType?,
    val isFreeze: Boolean?,
    val mouvementType: InvoiceMouvementType?
): IQueryExtend<Invoice> {

    override fun isStatisfy(entity: Invoice): Boolean {
        if (accountIds != null && !accountIds.contains(entity.accountId))
            return false

        if (types != null && !types.contains(entity.type))
            return false

        if (startDate != null &&  startDate <= entity.date)
            return false

        if (endDate != null &&  endDate >= entity.date)
            return false

        if (isFreeze != null && isFreeze != entity.isFreeze)
            return false

        if (mouvementType != null &&  mouvementType != entity.mouvementType)
            return false

        return true
    }
}