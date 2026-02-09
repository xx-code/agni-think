package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateProvisionableModel
import dev.auguste.agni_api.controllers.models.ApiUpdateProvisionableModel
import dev.auguste.agni_api.controllers.models.mapApiCreateProvisionable
import dev.auguste.agni_api.controllers.models.mapApiUpdateProvisionable
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionableInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionableInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionableOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionableInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/provisionables")
class ProvisionableController (
    val createProvisionableUseCase: IUseCase<CreateProvisionableInput, CreatedOutput>,
    val updateProvisionableUseCase: IUseCase<UpdateProvisionableInput, Unit>,
    val deleteProvisionableUseCase: IUseCase<DeleteProvisionableInput, Unit>,
    val getProvisionableUseCase: IUseCase<UUID, GetProvisionableOutput>,
    val getAllProvisionablesUseCase: IUseCase<QueryFilter, ListOutput<GetProvisionableOutput>>
) {
    @PostMapping
    fun createProvisionable(@Valid @RequestBody request: ApiCreateProvisionableModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createProvisionableUseCase.execAsync(
            mapApiCreateProvisionable(request)
        ))
    }

    @PostMapping("/{id}")
    fun updateProvisionable(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateProvisionableModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateProvisionableUseCase.execAsync(
            mapApiUpdateProvisionable(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteProvisionable(@PathVariable id: UUID): ResponseEntity<Unit> {
       return ResponseEntity.ok(deleteProvisionableUseCase.execAsync(
           DeleteProvisionableInput(id)
       ))
    }

    @GetMapping("/{id}")
    fun getProvisionable(@PathVariable id: UUID) : ResponseEntity<GetProvisionableOutput> {
        return ResponseEntity.ok(getProvisionableUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllProvisionables(query: QueryFilter) : ResponseEntity<ListOutput<GetProvisionableOutput>> {
        return ResponseEntity.ok(getAllProvisionablesUseCase.execAsync(
            query
        ))
    }
}