package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("budgets")
data class JdbcBudgetModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("budget_id")
    val id: UUID,

    @Column("title")
    val name: String,

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
        return id
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
            title = model.name,
            target = model.target,
            scheduler = Scheduler.fromMap(schedulerJson),
            isArchived = model.isArchived,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }

    override fun toModel(entity: Budget): JdbcBudgetModel {
        return JdbcBudgetModel(
            id = entity.id,
            name = entity.title,
            target = entity.target,
            scheduler = objectMapper.writeValueAsString(entity.scheduler.toMap()),
            isArchived = entity.isArchived,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun getTableName(): String {
        TODO("Not yet implemented")
    }

    override fun getSortField(): Set<String> {
        return setOf("target", "created_at", "updated_at")
    }

    override fun getModelClass(): Class<JdbcBudgetModel> {
        TODO("Not yet implemented")
    }
}