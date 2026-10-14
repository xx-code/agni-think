package usecases.schedule_Invoices.dto

import adapters.dto.QueryFilter

data class GetAllScheduleInvoiceInput(
    val queryFilter: QueryFilter
)
