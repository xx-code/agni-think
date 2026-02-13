package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class ApiScheduleInvoiceModel(
    @field:NotNull("Due date must be null or empty")
    val dueDate: LocalDateTime,
    val repeater: ApiScheduleRepeaterModel? = null,
)

data class ApiCreateScheduleInvoiceModel(
    @field:NotEmpty("name must not be empty")
    val name: String,
    @field:NotNull("accountId must not be null or empty")
    val accountId: UUID,
    @field:Min(0, "amount must be positive")
    val amount: Double,
    @field:NotEmpty( "description must not be empty")
    val description: String,
    val isFreeze: Boolean?,
    val categoryId: UUID?,
    val tagIds: Set<UUID>,
    val type: String?,
    @field:NotNull("schedule must be defined")
    val schedule: ApiScheduleInvoiceModel
)

data class ApiUpdateScheduleInvoiceModel(
    val name: String?,
    val amount: Double?,
    val accountId: UUID?,
    val description: String?,
    val categoryId: UUID?,
    val tagIds: Set<UUID>,
    val type: String?,
    val isPause: Boolean?,
    val schedule: ApiScheduleInvoiceModel?
)

fun mapApiCreateScheduleInvoice(model: ApiCreateScheduleInvoiceModel): CreateScheduleInvoiceInput {
    return CreateScheduleInvoiceInput(
        name = model.name,
        accountId = model.accountId,
        amount = model.amount,
        description = model.description,
        isFreeze = model.isFreeze,
        categoryId = model.categoryId,
        tagIds = model.tagIds,
        type = model.type?.let {
            InvoiceType.fromString(model.type)
       },
        schedule = SchedulerInvoiceInput(
            dueDate = model.schedule.dueDate,
            repeater = model.schedule.repeater?.let {
                ScheduleRepeaterInput(
                    period = PeriodType.fromString(model.schedule.repeater.period),
                    interval = model.schedule.repeater.interval
                )
            }
        )
    )
}

fun mapApiUpdateScheduleInvoice(id: UUID, model: ApiUpdateScheduleInvoiceModel): UpdateScheduleInvoiceInput {
    return UpdateScheduleInvoiceInput(
        id = id,
        name = model.name,
        accountId = model.accountId,
        amount = model.amount,
        categoryId = model.categoryId,
        tagIds = model.tagIds,
        type = model.type?.let { InvoiceType.fromString(model.type) },
        isPause = model.isPause,
        schedule = model.schedule?.let {
            SchedulerInvoiceInput(
                dueDate = model.schedule.dueDate,
                repeater = model.schedule.repeater?.let {
                    ScheduleRepeaterInput(
                        period = PeriodType.fromString(model.schedule.repeater.period) ,
                        interval = model.schedule.repeater.interval
                    )
                }
            )
        }
    )
}