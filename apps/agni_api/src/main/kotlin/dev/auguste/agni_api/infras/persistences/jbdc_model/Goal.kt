package dev.auguste.agni_api.infras.persistences.jbdc_model

import domain.entities.Goal
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("goals")
data class JdbcGoalModel(
    @Id
    @get:JvmName("getIdentifier")
    val goalId: UUID,
    val title: String,
    @Column("source_id")
    val sourceId: UUID,
    val description: String,
    @Column("due_date")
    val dueDate: LocalDate,
    @Column("target_amount")
    val targetAmount: Double,
    val status: Int,
    val type: String
) : JdbcModel() {
    override fun getId(): UUID {
        return goalId
    }
}

@Component
class JdbcGoalModelMapper: IMapper<JdbcGoalModel, Goal> {
    override fun toDomain(model: JdbcGoalModel): Goal {
        return Goal(
            id = model.id,
            title = model.title,
            description = model.description,
            targetSourceId = model.sourceId,
            targetAmount = model.targetAmount,
            dueDate = model.dueDate,
            status = GoalStatusType.fromInt(model.status),
            type = GoalEvaluationType.fromString(model.type)
        )
    }

    override fun toModel(entity: Goal): JdbcGoalModel {
        return JdbcGoalModel(
            goalId = entity.id,
            title = entity.title,
            sourceId = entity.targetSourceId,
            description = entity.description,
            targetAmount = entity.targetAmount,
            dueDate = entity.dueDate,
            status = entity.status.ordinal,
            type = entity.type.value
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "goal_id",
        "title" to "title",
        "description" to "description",
        "targetSourceId" to "source_id",
        "targetAmount" to "target_amount",
        "dueDate" to "due_date",
        "status" to "status",
        "type" to "type"
    )

    override fun getTableName(): String = "goals"

    override fun getSortField(): Set<String> {
        return setOf("due_date", "target_amount", "status", "type")
    }

    override fun getModelClass(): Class<JdbcGoalModel> = JdbcGoalModel::class.java
}
