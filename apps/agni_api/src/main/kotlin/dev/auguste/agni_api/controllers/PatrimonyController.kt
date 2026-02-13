package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiAddSnapshotToPatrimonyModel
import dev.auguste.agni_api.controllers.models.ApiCreatePatrimonyModel
import dev.auguste.agni_api.controllers.models.ApiUpdatePatrimonyModel
import dev.auguste.agni_api.controllers.models.ApiUpdateSnapshotFromPatrimonyModel
import dev.auguste.agni_api.controllers.models.mapApiAddSnapshotToPatrimony
import dev.auguste.agni_api.controllers.models.mapApiCreatePatrimony
import dev.auguste.agni_api.controllers.models.mapApiUpdatePatrimony
import dev.auguste.agni_api.controllers.models.mapApiUpdateSnapshotToPatrimony
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.dto.CreatePatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.DeletePatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.UpdatePatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/patrimonies")
class PatrimonyController(
    private val createPatrimonyUseCase: IUseCase<CreatePatrimonyInput, CreatedOutput>,
    private val updatePatrimonyUseCase: IUseCase<UpdatePatrimonyInput, Unit>,
    private val deletePatrimonyUseCase: IUseCase<DeletePatrimonyInput, Unit>,
    private val getPatrimonyUseCase: IUseCase<UUID, GetPatrimonyOutput>,
    private val getAllPatrimoniesUseCase: IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>>,
    private val addSnapshotToPatrimonyUseCase: IUseCase<AddSnapshotToPatrimonyInput, CreatedOutput>,
    private val removeSnapshotFromPatrimonyUseCase: IUseCase<RemoveSnapshotFromPatrimonyInput, Unit>,
    private val updateSnapshotFromPatrimonyUseCase: IUseCase<UpdateSnapshotFromPatrimonyInput, Unit>,
    private val getAllPatrimonySnapshotUseCase: IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>>
) {

    @PostMapping
    fun createPatrimony(@Valid @RequestBody request: ApiCreatePatrimonyModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createPatrimonyUseCase.execAsync(
            mapApiCreatePatrimony(request)
        ))
    }

    @PutMapping("/{id}")
    fun updatePatrimony(@PathVariable id:UUID, @Valid @RequestBody request: ApiUpdatePatrimonyModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updatePatrimonyUseCase.execAsync(
            mapApiUpdatePatrimony(id,request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deletePatrimony(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deletePatrimonyUseCase.execAsync(
            DeletePatrimonyInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getPatrimony(@PathVariable id: UUID) : ResponseEntity<GetPatrimonyOutput> {
        return ResponseEntity.ok(getPatrimonyUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllPatrimonies(query: QueryFilter) : ResponseEntity<ListOutput<GetPatrimonyOutput>> {
        return ResponseEntity.ok(getAllPatrimoniesUseCase.execAsync(
            query
        ))
    }

    @PostMapping("/{id}/add-snapshot")
    fun addSnapshotToPatrimonies(@PathVariable id: UUID, @Valid @RequestBody request: ApiAddSnapshotToPatrimonyModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(addSnapshotToPatrimonyUseCase.execAsync(
            mapApiAddSnapshotToPatrimony(id, request)
        ))
    }

    @PutMapping("/remove-snapshot/{snapshotId}")
    fun removeSnapshotFromPatrimonies(@PathVariable snapshotId: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(removeSnapshotFromPatrimonyUseCase.execAsync(
            RemoveSnapshotFromPatrimonyInput(snapshotId)
        ))
    }

    @PutMapping("/update-snapshot/{snapshotId}")
    fun updateSnapshotFromPatrimonies(@PathVariable snapshotId: UUID, @Valid @RequestBody request: ApiUpdateSnapshotFromPatrimonyModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSnapshotFromPatrimonyUseCase.execAsync(
            mapApiUpdateSnapshotToPatrimony(snapshotId, request)
        ))
    }

    @GetMapping("/{id}/snapshots")
    fun getSnapshotsFromPatrimony(@PathVariable id: UUID, query: QueryFilter) : ResponseEntity<ListOutput<GetSnapshotPatrimonyOutput>> {
        return ResponseEntity.ok(getAllPatrimonySnapshotUseCase.execAsync(
            GetAllSnapshotPatrimonyInput(id, query)
        ))
    }
}