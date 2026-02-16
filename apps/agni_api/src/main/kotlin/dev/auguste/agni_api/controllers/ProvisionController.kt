package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateProvisionModel
import dev.auguste.agni_api.controllers.models.ApiUpdateProvisionModel
import dev.auguste.agni_api.controllers.models.mapApiCreateProvision
import dev.auguste.agni_api.controllers.models.mapApiUpdateProvision
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
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
@RequestMapping("/v2/provisions")
class ProvisionController (
    val createProvisionUseCase: IUseCase<CreateProvisionInput, CreatedOutput>,
    val updateProvisionUseCase: IUseCase<UpdateProvisionInput, Unit>,
    val deleteProvisionUseCase: IUseCase<DeleteProvisionInput, Unit>,
    val getProvisionUseCase: IUseCase<UUID, GetProvisionOutput>,
    val getAllProvisionUseCase: IUseCase<QueryFilter, ListOutput<GetProvisionOutput>>
) {
    @PostMapping
    fun createProvision(@Valid @RequestBody request: ApiCreateProvisionModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createProvisionUseCase.execAsync(
            mapApiCreateProvision(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateProvision(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateProvisionModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateProvisionUseCase.execAsync(
            mapApiUpdateProvision(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteProvision(@PathVariable id: UUID): ResponseEntity<Unit> {
       return ResponseEntity.ok(deleteProvisionUseCase.execAsync(
           DeleteProvisionInput(id)
       ))
    }

    @GetMapping("/{id}")
    fun getProvision(@PathVariable id: UUID) : ResponseEntity<GetProvisionOutput> {
        return ResponseEntity.ok(getProvisionUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllProvisions(query: QueryFilter) : ResponseEntity<ListOutput<GetProvisionOutput>> {
        return ResponseEntity.ok(getAllProvisionUseCase.execAsync(
            query
        ))
    }
}