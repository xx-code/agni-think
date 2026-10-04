package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateGoal
import dev.auguste.rest_api.controllers.models.ApiGaolQueryExtend
import dev.auguste.rest_api.controllers.models.ApiUpdateGoal
import dev.auguste.rest_api.controllers.models.mapApiCreateGoal
import dev.auguste.rest_api.controllers.models.mapApiUpdateGoal
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.goals.dto.CreateGoalInput
import usecases.goals.dto.GetAllGoalInput
import usecases.goals.dto.GetGoalOutput
import usecases.goals.dto.UpdateGoalInput
import usecases.interfaces.IUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/v2/goals")
class GoalController(
    private val createGoal: IUseCase<CreateGoalInput, CreatedOutput>,
    private val updateGoal: IUseCase<UpdateGoalInput, Unit>,
    private val getGoal: IUseCase<UUID, GetGoalOutput>,
    private val getAllGoal: IUseCase<GetAllGoalInput, ListOutput<GetGoalOutput>>,
    private val deleteGoal: IUseCase<UUID, Unit>
) {

    @PostMapping
    suspend fun createGoal(@Valid @RequestBody input: ApiCreateGoal): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(
            createGoal.execute(mapApiCreateGoal(input)).getOrThrow()
        )
    }

    @PutMapping("/{id}")
    suspend fun updateGoal(@PathVariable id: UUID, @Valid @RequestBody input: ApiUpdateGoal): ResponseEntity<Unit> {
        return ResponseEntity.ok(
            updateGoal.execute(mapApiUpdateGoal(id, input)).getOrThrow()
        )
    }

    @GetMapping("/{id}")
    suspend fun getGoal(@PathVariable id: UUID): ResponseEntity<GetGoalOutput> {
        return ResponseEntity.ok(
            getGoal.execute(id).getOrThrow()
        )
    }

    @GetMapping
    suspend fun getAllGoals(@ModelAttribute query: QueryFilter, @ModelAttribute queryExtend: ApiGaolQueryExtend): ResponseEntity<ListOutput<GetGoalOutput>> {
        return ResponseEntity.ok(
            getAllGoal.execute(GetAllGoalInput(
                query,
                sourceId = queryExtend.sourceId,
                status = queryExtend.status?.let { GoalStatusType.fromInt(it) },
                type = queryExtend.type?.let { GoalEvaluationType.fromString(it) }
            )).getOrThrow()
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteGoal(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteGoal.execute(id).getOrThrow())
    }
}