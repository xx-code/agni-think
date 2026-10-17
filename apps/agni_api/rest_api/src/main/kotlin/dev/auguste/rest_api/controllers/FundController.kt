package dev.auguste.rest_api.controllers

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateFundModel
import dev.auguste.rest_api.controllers.models.ApiDeleteFundModel
import dev.auguste.rest_api.controllers.models.ApiUpdateFundModel
import dev.auguste.rest_api.controllers.models.ApiUpgradeFundModel
import domain.enums.FundType
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.funds.dto.CreateFundInput
import usecases.funds.dto.DecreaseFundInput
import usecases.funds.dto.DeleteFundInput
import usecases.funds.dto.GetAllFundInput
import usecases.funds.dto.GetFundOutput
import usecases.funds.dto.IncreaseFundInput
import usecases.funds.dto.UpdateFundInput
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
class FundController (
    private val createSavingGoalUseCase: IUseCase<CreateFundInput, CreatedOutput>,
    private val updateSavingGoalUseCase: IUseCase<UpdateFundInput, Unit>,
    private val deleteSavingGoalUseCase: IUseCase<DeleteFundInput, Unit>,
    private val getSavingGoalUseCase: IUseCase<UUID, GetFundOutput>,
    private val getAllSavingGoalUseCase: IUseCase<GetAllFundInput, ListOutput<GetFundOutput>>,
    private val increaseSavingGoalUseCase: IUseCase<IncreaseFundInput, Unit>,
    private val decreaseSavingGoalUseCase: IUseCase<DecreaseFundInput, Unit>,
    private val auth: IAuthenticateAdapter
){

    @PostMapping
    suspend fun createSavingGoal(@Valid @RequestBody request: ApiCreateFundModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createSavingGoalUseCase.execute(
            request.toCreateFundInput()
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateFundModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSavingGoalUseCase.execute(
            request.toUpdateFundInput(id)
        ).getOrThrow())
    }

    @PutMapping("/{id}/remove")
    suspend fun deleteSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiDeleteFundModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteSavingGoalUseCase.execute(
            DeleteFundInput(id, request.accountId)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getSavingGoal(@PathVariable id: UUID): ResponseEntity<GetFundOutput> {
        return ResponseEntity.ok(getSavingGoalUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllSavingGoal(query: QueryFilter, type: String? = null): ResponseEntity<ListOutput<GetFundOutput>> {
        return ResponseEntity.ok(getAllSavingGoalUseCase.execute(GetAllFundInput(
            query,
            if (!type.isNullOrEmpty()) FundType.fromString(type) else null
        )).getOrThrow())
    }

    @PutMapping("/{id}/increase")
    suspend fun increaseSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpgradeFundModel): ResponseEntity<Unit> {
        val currentUser = auth.getCurrentUser()
        return ResponseEntity.ok(increaseSavingGoalUseCase.execute(
            IncreaseFundInput(
                fundId = id,
                userId = currentUser.id,
                accountId = request.accountId,
                amount = request.amount
            )
        ).getOrThrow())
    }

    @PutMapping("/{id}/decrease")
    suspend fun decreaseSavingGoal(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpgradeFundModel): ResponseEntity<Unit> {
        val currentUser = auth.getCurrentUser()
        return ResponseEntity.ok(decreaseSavingGoalUseCase.execute(
            DecreaseFundInput(
                fundId = id,
                userId = currentUser.id,
                accountId = request.accountId,
                amount = request.amount
            )
        ).getOrThrow())
    }
}