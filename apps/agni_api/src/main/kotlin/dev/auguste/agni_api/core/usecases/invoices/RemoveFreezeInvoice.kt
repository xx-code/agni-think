package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventContent
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import java.time.LocalDateTime

class RemoveFreezeInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val accountRepo: IRepository<Account>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val eventRegister: IEventRegister
): IUseCase<Unit, BackgroundTaskOut> {
    override fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val freezeInvoice = invoiceRepo.getAll(
                query = QueryFilter(0, 0, true),
                queryExtend = QueryInvoiceExtend(
                    accountIds = null,
                    endDate = LocalDateTime.now() ,
                    startDate = null,
                    types = null,
                    isFreeze = true,
                    status = InvoiceStatusType.COMPLETED,
                    mouvementType = null
                )
            )

            for(invoiceItem in freezeInvoice.items) {
                deleteInvoice.execAsync(DeleteInvoiceInput(invoiceItem.id))
                val account = accountRepo.get(invoiceItem.accountId)

                eventRegister.notify("notification", EventContent(
                    "Transaction degeler",
                    "Transaction ${account?.title} a une transaction geler"
                ))
            }

            return BackgroundTaskOut("Remove Freeze transaction successfully")

        } catch (error: Throwable) {
            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}