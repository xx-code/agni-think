package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateSavingGoalModel
import dev.auguste.rest_api.controllers.models.ApiDeleteSavingGoalModel
import dev.auguste.rest_api.controllers.models.ApiUpdateSavingGoalModel
import dev.auguste.rest_api.controllers.models.ApiUpgradeSavingGoalModel
import dev.auguste.rest_api.controllers.models.mapApiCreateSavingGoal
import dev.auguste.rest_api.controllers.models.mapApiUpdateSavingGoal
import domain.enums.FundType
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.saving_goals.dto.CreateSavingGoalInput
import usecases.saving_goals.dto.DecreaseSavingGoalInput
import usecases.saving_goals.dto.DeleteSavingGoalInput
import usecases.saving_goals.dto.GetAllSavingGoalInput
import usecases.saving_goals.dto.GetSavingGoalOutput
import usecases.saving_goals.dto.IncreaseSavingGoalInput
import usecases.saving_goals.dto.UpdateSavingGoalInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/funds")
class SavingGoalController (
    private val createSavingGoalUseCase: IUseCase<CreateSavingGoalInput, CreatedOutput>,
    private val updateSavingGoalUseCase: IUseCase<UpdateSavingGoalInput, Unit>,
    private val deleteSavingGoalUseCase: IUseCase<DeleteSavingGoalInput, Unit>,
    private val getSavingGoalUseCase: IUseCase<UUID, GetSavingGoalOutput>,
    private val getAllSavingGoalUseCase: IUseCase<GetAllSavingGoalInput, ListOutput<GetSavingGoalOutput>>,
    private val increaseSavingGoalUseCase: IUseCase<IncreaseSavingGoalInput, Unit>,
    private val decreaseSavingGoalUseCase: IUseCase<DecreaseSavingGoalInput, Unit>,
){

    @PostMapping
    suspend fun createSavingGoal(@Valid @RequestBody request: ApiCreateSavingGoalModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createSavingGoalUseCase.execute(
            mapApiCreateSavingGoal(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSavingGoalUseCase.execute(
            mapApiUpdateSavingGoal(id, request)
        ).getOrThrow())
    }

    @PutMapping("/{id}/remove")
    suspend fun deleteSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiDeleteSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteSavingGoalUseCase.execute(
            DeleteSavingGoalInput(id, request.accountId)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getSavingGoal(@PathVariable id: UUID): ResponseEntity<GetSavingGoalOutput> {
        return ResponseEntity.ok(getSavingGoalUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllSavingGoal(query: QueryFilter, type: String? = null): ResponseEntity<ListOutput<GetSavingGoalOutput>> {
        return ResponseEntity.ok(getAllSavingGoalUseCase.execute(GetAllSavingGoalInput(
            query,
            if (!type.isNullOrEmpty()) FundType.fromString(type) else null
        )).getOrThrow())
    }

    @PutMapping("/{id}/increase")
    suspend fun increaseSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpgradeSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(increaseSavingGoalUseCase.execute(
            IncreaseSavingGoalInput(
                savingGoalId = id,
                accountId = request.accountId,
                amount = request.amount
            )
        ).getOrThrow())
    }

    @PutMapping("/{id}/decrease")
    suspend fun decreaseSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpgradeSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(decreaseSavingGoalUseCase.execute(
            DecreaseSavingGoalInput(
                savingGoalId = id,
                accountId = request.accountId,
                amount = request.amount
            )
        ).getOrThrow())
    }
}