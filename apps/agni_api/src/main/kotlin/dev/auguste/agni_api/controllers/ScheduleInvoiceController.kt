package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateScheduleInvoiceModel
import dev.auguste.agni_api.controllers.models.ApiUpdateScheduleInvoiceModel
import dev.auguste.agni_api.controllers.models.mapApiCreateScheduleInvoice
import dev.auguste.agni_api.controllers.models.mapApiUpdateScheduleInvoice
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/schedule-invoices")
class ScheduleInvoiceController (
    private val createScheduleInvoiceUseCase: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
    private val updateScheduleInvoiceUseCase: IUseCase<UpdateScheduleInvoiceInput, Unit>,
    private val deleteScheduleInvoiceUseCase: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    private val getScheduleInvoiceUseCase: IUseCase<UUID, GetScheduleInvoiceOutput>,
    private val getAllScheduleInvoiceUseCase: IUseCase<QueryFilter, ListOutput<GetScheduleInvoiceOutput>>
){

    @PostMapping
    fun createScheduleInvoice(@Valid @RequestBody request: ApiCreateScheduleInvoiceModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createScheduleInvoiceUseCase.execAsync(
            mapApiCreateScheduleInvoice(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateScheduleInvoice(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateScheduleInvoiceModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateScheduleInvoiceUseCase.execAsync(
            mapApiUpdateScheduleInvoice(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteScheduleInvoice(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteScheduleInvoiceUseCase.execAsync(
            DeleteScheduleInvoiceInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getScheduleInvoice(@PathVariable id: UUID): ResponseEntity<GetScheduleInvoiceOutput> {
        return ResponseEntity.ok(getScheduleInvoiceUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllScheduleInvoice(query: QueryFilter): ResponseEntity<ListOutput<GetScheduleInvoiceOutput>> {
        return ResponseEntity.ok(getAllScheduleInvoiceUseCase.execAsync(
            query
        ))
    }
}