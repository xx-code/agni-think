package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiAddSnapshotToPatrimonyModel
import dev.auguste.rest_api.controllers.models.ApiCreatePatrimonyModel
import dev.auguste.rest_api.controllers.models.ApiUpdatePatrimonyModel
import dev.auguste.rest_api.controllers.models.ApiUpdateSnapshotFromPatrimonyModel
import dev.auguste.rest_api.controllers.models.mapApiAddSnapshotToPatrimony
import dev.auguste.rest_api.controllers.models.mapApiCreatePatrimony
import dev.auguste.rest_api.controllers.models.mapApiUpdatePatrimony
import dev.auguste.rest_api.controllers.models.mapApiUpdateSnapshotToPatrimony
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.patrimonies.dto.CreatePatrimonyInput
import usecases.patrimonies.dto.DeletePatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyOutput
import usecases.patrimonies.dto.SourcePatrimonyType
import usecases.patrimonies.dto.UpdatePatrimonyInput
import usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput
import usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput
import usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput
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
@RequestMapping("/v2/patrimonies")
class PatrimonyController(
    private val createPatrimonyUseCase: IUseCase<CreatePatrimonyInput, CreatedOutput>,
    private val updatePatrimonyUseCase: IUseCase<UpdatePatrimonyInput, Unit>,
    private val deletePatrimonyUseCase: IUseCase<DeletePatrimonyInput, Unit>,
    private val getPatrimonyUseCase: IUseCase<GetPatrimonyInput, GetPatrimonyOutput>,
    private val getAllPatrimoniesUseCase: IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>>,
    private val addSnapshotToPatrimonyUseCase: IUseCase<AddSnapshotToPatrimonyInput, CreatedOutput>,
    private val removeSnapshotFromPatrimonyUseCase: IUseCase<RemoveSnapshotFromPatrimonyInput, Unit>,
    private val updateSnapshotFromPatrimonyUseCase: IUseCase<UpdateSnapshotFromPatrimonyInput, Unit>,
    private val getAllPatrimonySnapshotUseCase: IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>>
) {

    @PostMapping
    suspend fun createPatrimony(@Valid @RequestBody request: ApiCreatePatrimonyModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createPatrimonyUseCase.execute(
            mapApiCreatePatrimony(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updatePatrimony(@PathVariable id:UUID, @Valid @RequestBody request: ApiUpdatePatrimonyModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updatePatrimonyUseCase.execute(
            mapApiUpdatePatrimony(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deletePatrimony(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deletePatrimonyUseCase.execute(
            DeletePatrimonyInput(id)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getPatrimony(@PathVariable id: UUID, sourceType: String = SourcePatrimonyType.PATRIMONY.value, isAsset: Boolean = false) : ResponseEntity<GetPatrimonyOutput> {
        return ResponseEntity.ok(getPatrimonyUseCase.execute(
            GetPatrimonyInput(id, SourcePatrimonyType.fromString(sourceType), isAsset)
        ).getOrThrow())
    }
    @GetMapping("/total-fund")
    suspend fun getPatrimony() : ResponseEntity<GetPatrimonyOutput> {
        return ResponseEntity.ok(getPatrimonyUseCase.execute(
            GetPatrimonyInput(UUID.randomUUID(), SourcePatrimonyType.FUND)
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllPatrimonies(query: QueryFilter) : ResponseEntity<ListOutput<GetPatrimonyOutput>> {
        return ResponseEntity.ok(getAllPatrimoniesUseCase.execute(
            query
        ).getOrThrow())
    }

    @PostMapping("/{id}/add-snapshot")
    suspend fun addSnapshotToPatrimonies(@PathVariable id: UUID, @Valid @RequestBody request: ApiAddSnapshotToPatrimonyModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(addSnapshotToPatrimonyUseCase.execute(
            mapApiAddSnapshotToPatrimony(id, request)
        ).getOrThrow())
    }

    @PutMapping("/remove-snapshot/{snapshotId}")
    suspend fun removeSnapshotFromPatrimonies(@PathVariable snapshotId: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(removeSnapshotFromPatrimonyUseCase.execute(
            RemoveSnapshotFromPatrimonyInput(snapshotId)
        ).getOrThrow())
    }

    @PutMapping("/update-snapshot/{snapshotId}")
    suspend fun updateSnapshotFromPatrimonies(@PathVariable snapshotId: UUID, @Valid @RequestBody request: ApiUpdateSnapshotFromPatrimonyModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSnapshotFromPatrimonyUseCase.execute(
            mapApiUpdateSnapshotToPatrimony(snapshotId, request)
        ).getOrThrow())
    }

    @GetMapping("/{id}/snapshots")
    suspend fun getSnapshotsFromPatrimony(@PathVariable id: UUID, query: QueryFilter, sourceType: String = SourcePatrimonyType.PATRIMONY.value, isAsset: Boolean = false) : ResponseEntity<ListOutput<GetSnapshotPatrimonyOutput>> {
        return ResponseEntity.ok(getAllPatrimonySnapshotUseCase.execute(
            GetAllSnapshotPatrimonyInput(id, query, SourcePatrimonyType.fromString(sourceType), isAsset)
        ).getOrThrow())
    }
}