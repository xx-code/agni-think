package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateScheduleInvoiceModel
import dev.auguste.rest_api.controllers.models.ApiUpdateScheduleInvoiceModel
import dev.auguste.rest_api.controllers.models.mapApiCreateScheduleInvoice
import dev.auguste.rest_api.controllers.models.mapApiUpdateScheduleInvoice
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
    suspend fun createScheduleInvoice(@Valid @RequestBody request: ApiCreateScheduleInvoiceModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createScheduleInvoiceUseCase.execute(
            mapApiCreateScheduleInvoice(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateScheduleInvoice(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateScheduleInvoiceModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateScheduleInvoiceUseCase.execute(
            mapApiUpdateScheduleInvoice(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteScheduleInvoice(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteScheduleInvoiceUseCase.execute(
            DeleteScheduleInvoiceInput(id)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getScheduleInvoice(@PathVariable id: UUID): ResponseEntity<GetScheduleInvoiceOutput> {
        return ResponseEntity.ok(getScheduleInvoiceUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllScheduleInvoice(query: QueryFilter): ResponseEntity<ListOutput<GetScheduleInvoiceOutput>> {
        return ResponseEntity.ok(getAllScheduleInvoiceUseCase.execute(
            query
        ).getOrThrow())
    }
}