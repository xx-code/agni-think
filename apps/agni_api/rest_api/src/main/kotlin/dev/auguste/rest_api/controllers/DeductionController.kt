package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateDeductionModel
import dev.auguste.rest_api.controllers.models.ApiUpdateDeductionModel
import dev.auguste.rest_api.controllers.models.mapApiCreateDeduction
import dev.auguste.rest_api.controllers.models.mapApiUpdateDeduction
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.deductions.dto.CreateDeductionInput
import usecases.deductions.dto.DeleteDeductionInput
import usecases.deductions.dto.GetDeductionOutput
import usecases.deductions.dto.UpdateDeductionInput
import usecases.interfaces.IUseCase
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
@RequestMapping("/v2/deductions")
class DeductionController(
    val createDeductionUseCase: IUseCase<CreateDeductionInput, CreatedOutput>,
    val updateDeductionUseCase: IUseCase<UpdateDeductionInput, Unit>,
    val deleteDeductionUseCase: IUseCase<DeleteDeductionInput, Unit>,
    val getDeductionUseCase: IUseCase<UUID, GetDeductionOutput>,
    val getAllDeductionUseCase: IUseCase<QueryFilter, ListOutput<GetDeductionOutput>>
) {

    @PostMapping
    suspend fun createDeduction(@Valid @RequestBody request: ApiCreateDeductionModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createDeductionUseCase.execute(
            mapApiCreateDeduction(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateDeduction(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateDeductionModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateDeductionUseCase.execute(
            mapApiUpdateDeduction(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteDeduction(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteDeductionUseCase.execute(
            DeleteDeductionInput(id)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getDeduction(@PathVariable id: UUID) : ResponseEntity<GetDeductionOutput> {
        return ResponseEntity.ok(getDeductionUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllDeduction(query: QueryFilter) : ResponseEntity<ListOutput<GetDeductionOutput>> {
        return ResponseEntity.ok(getAllDeductionUseCase.execute(
            query
        ).getOrThrow())
    }
}