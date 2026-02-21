package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateIncomeSourceModel
import dev.auguste.agni_api.controllers.models.ApiUpdateIncomeSourceModel
import dev.auguste.agni_api.controllers.models.mapApiCreateIncomeSourceTo
import dev.auguste.agni_api.controllers.models.mapApiUpdateIncomeSourceTo
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.CreateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.DeleteIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.GetIncomeSourceOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.UpdateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
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
@RequestMapping("/v2/income-sources")
class IncomeSourceController(
    private val createIncomeSource: IUseCase<CreateIncomeSourceInput, CreatedOutput>,
    private val updateIncomeSource: IUseCase<UpdateIncomeSourceInput, Unit>,
    private val deleteIncomeSource: IUseCase<DeleteIncomeSourceInput, Unit>,
    private val getIncomeSource: IUseCase<UUID, GetIncomeSourceOutput>,
    private val getAllIncomeSource: IUseCase<QueryFilter, ListOutput<GetIncomeSourceOutput>>
) {

    @PostMapping
    fun createIncomeSource(@RequestBody request: ApiCreateIncomeSourceModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createIncomeSource.execAsync(
            mapApiCreateIncomeSourceTo(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateIncomeSource(@PathVariable id: UUID, @RequestBody request: ApiUpdateIncomeSourceModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateIncomeSource.execAsync(
            mapApiUpdateIncomeSourceTo(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteIncomeSource(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteIncomeSource.execAsync(
            DeleteIncomeSourceInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getIncomeSource(@PathVariable id: UUID): ResponseEntity<GetIncomeSourceOutput> {
        return ResponseEntity.ok(getIncomeSource.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllIncomeSource(query: QueryFilter): ResponseEntity<ListOutput<GetIncomeSourceOutput>> {
        return ResponseEntity.ok(getAllIncomeSource.execAsync(query))
    }
}