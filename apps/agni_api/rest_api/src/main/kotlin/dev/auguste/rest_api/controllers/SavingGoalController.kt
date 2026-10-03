package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateSavingGoalModel
import dev.auguste.rest_api.controllers.models.ApiDeleteSavingGoalModel
import dev.auguste.rest_api.controllers.models.ApiUpdateSavingGoalModel
import dev.auguste.rest_api.controllers.models.ApiUpgradeSavingGoalModel
import dev.auguste.rest_api.controllers.models.mapApiCreateSavingGoal
import dev.auguste.rest_api.controllers.models.mapApiUpdateSavingGoal
import domain.enums.FundType
import usecases.CreatedOutput
import usecases.ListOutput
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
    fun createSavingGoal(@Valid @RequestBody request: ApiCreateSavingGoalModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createSavingGoalUseCase.execAsync(
            mapApiCreateSavingGoal(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSavingGoalUseCase.execAsync(
            mapApiUpdateSavingGoal(id, request)
        ))
    }

    @PutMapping("/{id}/remove")
    fun deleteSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiDeleteSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteSavingGoalUseCase.execAsync(
            DeleteSavingGoalInput(id, request.accountId)
        ))
    }

    @GetMapping("/{id}")
    fun getSavingGoal(@PathVariable id: UUID): ResponseEntity<GetSavingGoalOutput> {
        return ResponseEntity.ok(getSavingGoalUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllSavingGoal(query: QueryFilter, type: String? = null): ResponseEntity<ListOutput<GetSavingGoalOutput>> {
        return ResponseEntity.ok(getAllSavingGoalUseCase.execAsync(GetAllSavingGoalInput(
            query,
            if (!type.isNullOrEmpty()) FundType.fromString(type) else null
        )))
    }

    @PutMapping("/{id}/increase")
    fun increaseSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpgradeSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(increaseSavingGoalUseCase.execAsync(
            IncreaseSavingGoalInput(
                savingGoalId = id,
                accountId = request.accountId,
                amount = request.amount
            )
        ))
    }

    @PutMapping("/{id}/decrease")
    fun decreaseSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpgradeSavingGoalModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(decreaseSavingGoalUseCase.execAsync(
            DecreaseSavingGoalInput(
                savingGoalId = id,
                accountId = request.accountId,
                amount = request.amount
            )
        ))
    }
}