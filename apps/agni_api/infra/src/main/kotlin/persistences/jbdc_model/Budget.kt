package persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.Budget
import domain.value_objects.Scheduler
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDateTime
import java.util.UUID

@Table("budgets")
data class JdbcBudgetModel(
    @Id
    @get:JvmName("getIdentifier")
    val budgetId: UUID,

    val title: String,

    val target: Double,
    val scheduler: String,

    @Column("is_archived")
    val isArchived: Boolean,

    @Column("created_at")
    val createdAt: LocalDateTime,
    @Column("updated_at")
    val updatedAt: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return budgetId
    }
}

@Component
class JdbcBudgetModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcBudgetModel, Budget> {
    override fun toDomain(model: JdbcBudgetModel): Budget {
        val schedulerJson = objectMapper.readValue<Map<String, Any>?>(model.scheduler)

        return Budget(
            id = model.id,
            title = model.title,
            target = model.target,
            scheduler = Scheduler.fromMap(schedulerJson),
            isArchived = model.isArchived,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }

    override fun toModel(entity: Budget): JdbcBudgetModel {
        return JdbcBudgetModel(
            budgetId = entity.id,
            title = entity.title,
            target = entity.target,
            scheduler = objectMapper.writeValueAsString(entity.scheduler.toMap()),
            isArchived = entity.isArchived,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "budget_id",
        "title" to "title",
        "target" to "target",
        "scheduler" to "scheduler",
        "scheduler.date" to "scheduler->>'due_date'",
        "scheduler.repeater" to "scheduler->>'repeater'->>'period'",
        "isArchived" to "is_archived",
        "createdAt" to "created_at",
        "updatedAt" to "updated_at"
    )

    override fun getTableName(): String = "budgets"

    override fun getSortField(): Set<String> {
        return setOf("target", "created_at", "updated_at")
    }

    override fun getModelClass(): Class<JdbcBudgetModel> = JdbcBudgetModel::class.java
}
