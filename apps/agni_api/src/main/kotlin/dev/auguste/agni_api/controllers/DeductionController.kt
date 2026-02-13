package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateDeductionModel
import dev.auguste.agni_api.controllers.models.ApiUpdateDeductionModel
import dev.auguste.agni_api.controllers.models.mapApiCreateDeduction
import dev.auguste.agni_api.controllers.models.mapApiUpdateDeduction
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.deductions.dto.CreateDeductionInput
import dev.auguste.agni_api.core.usecases.deductions.dto.DeleteDeductionInput
import dev.auguste.agni_api.core.usecases.deductions.dto.GetDeductionOutput
import dev.auguste.agni_api.core.usecases.deductions.dto.UpdateDeductionInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
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
@RequestMapping("/v2/deductions")
class DeductionController(
    val createDeductionUseCase: IUseCase<CreateDeductionInput, CreatedOutput>,
    val updateDeductionUseCase: IUseCase<UpdateDeductionInput, Unit>,
    val deleteDeductionUseCase: IUseCase<DeleteDeductionInput, Unit>,
    val getDeductionUseCase: IUseCase<UUID, GetDeductionOutput>,
    val getAllDeductionUseCase: IUseCase<QueryFilter, ListOutput<GetDeductionOutput>>
) {

    @PostMapping
    fun createDeduction(@Valid @RequestBody request: ApiCreateDeductionModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createDeductionUseCase.execAsync(
            mapApiCreateDeduction(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateDeduction(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateDeductionModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateDeductionUseCase.execAsync(
            mapApiUpdateDeduction(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteDeduction(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteDeductionUseCase.execAsync(
            DeleteDeductionInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getDeduction(@PathVariable id: UUID) : ResponseEntity<GetDeductionOutput> {
        return ResponseEntity.ok(getDeductionUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllDeduction(query: QueryFilter) : ResponseEntity<ListOutput<GetDeductionOutput>> {
        return ResponseEntity.ok(getAllDeductionUseCase.execAsync(
            query
        ))
    }
}