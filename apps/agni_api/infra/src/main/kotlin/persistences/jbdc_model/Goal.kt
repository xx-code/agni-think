package persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.Goal
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.value_objects.SchedulerRecurrence
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
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
    val type: String,
    val recurrence: String?,
) : JdbcModel() {
    override fun getId(): UUID {
        return goalId
    }
}

@Component
class JdbcGoalModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcGoalModel, Goal> {
    override fun toDomain(model: JdbcGoalModel): Goal {
        //TODO: Centralize Code
        val recurrenceJson = if (
            model.recurrence == "null" || model.recurrence == "[null]" ||
            model.recurrence.isNullOrEmpty() || model.recurrence == "{}" || model.recurrence == "[]"
        ) { null }
        else {  objectMapper.readValue<Map<String, Any>>(model.recurrence) }


        return Goal(
            id = model.id,
            title = model.title,
            description = model.description,
            targetSourceId = model.sourceId,
            targetAmount = model.targetAmount,
            dueDate = model.dueDate,
            status = GoalStatusType.fromInt(model.status),
            type = GoalEvaluationType.fromString(model.type),
            recurrence = recurrenceJson?.let { SchedulerRecurrence.fromMap(it) },
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
            type = entity.type.value,
            recurrence = objectMapper.writeValueAsString(entity.recurrence?.toMap()),
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
        "type" to "type",
        "recurrence.period" to "'recurrence'->>'period'",
        "recurrence.interval" to "'recurrence'->>'interval'"
    )

    override fun getTableName(): String = "goals"

    override fun getSortField(): Set<String> {
        return setOf("due_date", "target_amount", "status", "type")
    }

    override fun getModelClass(): Class<JdbcGoalModel> = JdbcGoalModel::class.java
}
