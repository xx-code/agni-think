package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateProvisionModel
import dev.auguste.rest_api.controllers.models.ApiUpdateProvisionModel
import dev.auguste.rest_api.controllers.models.mapApiCreateProvision
import dev.auguste.rest_api.controllers.models.mapApiUpdateProvision
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.provisionable.dto.CreateProvisionInput
import usecases.provisionable.dto.DeleteProvisionInput
import usecases.provisionable.dto.GetProvisionOutput
import usecases.provisionable.dto.UpdateProvisionInput
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
    suspend fun createProvision(@Valid @RequestBody request: ApiCreateProvisionModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createProvisionUseCase.execute(
            mapApiCreateProvision(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateProvision(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateProvisionModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateProvisionUseCase.execute(
            mapApiUpdateProvision(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteProvision(@PathVariable id: UUID): ResponseEntity<Unit> {
       return ResponseEntity.ok(deleteProvisionUseCase.execute(
           DeleteProvisionInput(id)
       ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getProvision(@PathVariable id: UUID) : ResponseEntity<GetProvisionOutput> {
        return ResponseEntity.ok(getProvisionUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllProvisions(query: QueryFilter) : ResponseEntity<ListOutput<GetProvisionOutput>> {
        return ResponseEntity.ok(getAllProvisionUseCase.execute(
            query
        ).getOrThrow())
    }
}