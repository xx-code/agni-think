package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("budgets")
data class JdbcBudgetModel(
    @Id
    @Column("budget_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val target: Double,
    val scheduler: String,

    @Column("save_goal_ids")
    val goalIds: Set<UUID>
)

@Component
class JdbcBudgetModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcBudgetModel, Budget> {
    override fun toDomain(model: JdbcBudgetModel): Budget {
        val schedulerJson = objectMapper.readValue<Map<String, Any>?>(model.scheduler)

        return Budget(
            id = model.id,
            title = model.name,
            target = model.target,
            scheduler = Scheduler.fromMap(schedulerJson),
            targetSavingGoalIds = model.goalIds.toMutableSet()
        )
    }

    override fun toModel(entity: Budget): JdbcBudgetModel {
        return JdbcBudgetModel(
            id = entity.id,
            name = entity.title,
            target = entity.target,
            scheduler = objectMapper.writeValueAsString(entity.scheduler.toMap()),
            goalIds = entity.targetSavingGoalIds
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("target")
    }
}