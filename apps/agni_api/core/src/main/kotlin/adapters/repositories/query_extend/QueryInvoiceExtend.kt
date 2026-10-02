package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import java.time.LocalDateTime
import java.util.UUID

class QueryInvoiceExtend(
    val accountIds: Set<UUID>? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val types: Set<domain.enums.InvoiceType>? = null,
    val status: domain.enums.InvoiceStatusType? = null,
    val isFreeze: Boolean? = null,
    val mouvementType: domain.enums.InvoiceMovementType? = null,
): IQueryExtend<domain.entities.Invoice> {

    override fun isStatisfy(entity: domain.entities.Invoice): Boolean {
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

        if (mouvementType != null &&  mouvementType != entity.movementType)
            return false

        return true
    }
}