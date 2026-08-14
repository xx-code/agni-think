package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateGoal
import dev.auguste.agni_api.controllers.models.ApiGaolQueryExtend
import dev.auguste.agni_api.controllers.models.ApiUpdateGoal
import dev.auguste.agni_api.controllers.models.mapApiCreateGoal
import dev.auguste.agni_api.controllers.models.mapApiUpdateGoal
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.goals.dto.CreateGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.GetAllGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.GetGoalOutput
import dev.auguste.agni_api.core.usecases.goals.dto.UpdateGoalInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
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
import kotlin.uuid.toKotlinUuid


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
    fun createGoal(@Valid @RequestBody input: ApiCreateGoal): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(
            createGoal.execAsync(mapApiCreateGoal(input))
        )
    }

    @PutMapping("/{id}")
    fun updateGoal(@PathVariable id: UUID, @Valid @RequestBody input: ApiUpdateGoal): ResponseEntity<Unit> {
        return ResponseEntity.ok(
            updateGoal.execAsync(mapApiUpdateGoal(id,input))
        )
    }

    @GetMapping("/{id}")
    fun getGoal(@PathVariable id: UUID): ResponseEntity<GetGoalOutput> {
        return ResponseEntity.ok(
            getGoal.execAsync(id)
        )
    }

    @GetMapping
    fun getAllGoals(@ModelAttribute query: QueryFilter, @ModelAttribute queryExtend: ApiGaolQueryExtend): ResponseEntity<ListOutput<GetGoalOutput>> {
        return ResponseEntity.ok(
            getAllGoal.execAsync(GetAllGoalInput(
                query,
                sourceId = queryExtend.sourceId,
                status = queryExtend.status?.let { GoalStatusType.fromInt(it) },
                type = queryExtend.type?.let { GoalEvaluationType.fromString(it) }
            ))
        )
    }

    @DeleteMapping("/{id}")
    fun deleteGoal(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteGoal.execAsync(id))
    }
}