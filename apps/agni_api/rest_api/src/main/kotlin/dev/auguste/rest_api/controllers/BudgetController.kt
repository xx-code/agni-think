package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateBudgetModel
import dev.auguste.rest_api.controllers.models.ApiUpdateBudgetModel
import dev.auguste.rest_api.controllers.models.mapApiCreateBudgetModel
import dev.auguste.rest_api.controllers.models.mapApiUpdateBudgetModel
import domain.enums.PeriodType
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.budgets.dto.CreateBudgetInput
import usecases.budgets.dto.DeleteBudgetInput
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.budgets.dto.UpdateBudgetInput
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/budgets")
class BudgetController(
    private val createBudget: IUseCase<CreateBudgetInput, CreatedOutput>,
    private val updateBudget: IUseCase<UpdateBudgetInput, Unit>,
    private val deleteBudget: IUseCase<DeleteBudgetInput, Unit>,
    private val getBudget: IUseCase<UUID, GetBudgetOutput>,
    private val getAllBudgets: IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>
) {

    @PostMapping
    suspend fun createBudget(@Valid @RequestBody request: ApiCreateBudgetModel): ResponseEntity<CreatedOutput>  {
        return ResponseEntity.ok(createBudget.execute(
            mapApiCreateBudgetModel(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateBudget(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateBudgetModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateBudget.execute(
            mapApiUpdateBudgetModel(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteBudget(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteBudget.execute(
            DeleteBudgetInput(id)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getBudget(@PathVariable id: UUID): ResponseEntity<GetBudgetOutput> {
        return ResponseEntity.ok(getBudget.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllBudgets(query: QueryFilter, @RequestParam periodTypes: List<String>?): ResponseEntity<ListOutput<GetBudgetOutput>> {

        return ResponseEntity.ok(
            getAllBudgets.execute(GetAllBudgetInput(
                query,
                periodTypes?.map { period -> PeriodType.fromString(period)}?.toSet()
            )).getOrThrow()
        )
    }
}