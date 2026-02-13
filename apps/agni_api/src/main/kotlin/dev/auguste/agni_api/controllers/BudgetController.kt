package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateBudgetModel
import dev.auguste.agni_api.controllers.models.ApiUpdateBudgetModel
import dev.auguste.agni_api.controllers.models.mapApiCreateBudgetModel
import dev.auguste.agni_api.controllers.models.mapApiUpdateBudgetModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.CreateBudgetInput
import dev.auguste.agni_api.core.usecases.budgets.dto.DeleteBudgetInput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.UpdateBudgetInput
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
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/budgets")
class BudgetController(
    private val createBudget: IUseCase<CreateBudgetInput, CreatedOutput>,
    private val updateBudget: IUseCase<UpdateBudgetInput, Unit>,
    private val deleteBudget: IUseCase<DeleteBudgetInput, Unit>,
    private val getBudget: IUseCase<UUID, GetBudgetOutput>,
    private val getAllBudgets: IUseCase<QueryFilter, ListOutput<GetBudgetOutput>>
) {

    @PostMapping
    fun createBudget(@Valid @RequestBody request: ApiCreateBudgetModel): ResponseEntity<CreatedOutput>  {
        return ResponseEntity.ok(createBudget.execAsync(
            mapApiCreateBudgetModel(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateBudget(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateBudgetModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateBudget.execAsync(
            mapApiUpdateBudgetModel(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteBudget(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteBudget.execAsync(
            DeleteBudgetInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getBudget(@PathVariable id: UUID): ResponseEntity<GetBudgetOutput> {
        return ResponseEntity.ok(getBudget.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllBudgets(query: QueryFilter): ResponseEntity<ListOutput<GetBudgetOutput>> {
        return ResponseEntity.ok(getAllBudgets.execAsync(query))
    }
}