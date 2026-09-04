package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("spending_period_templates")
data class JdbcSpendingPeriodTemplateModel(
    @Id
    @get:JvmName("getIdentifier")
    val spendingPeriodTemplateId: UUID,
    @Column("is_active")
    val isActive: Boolean,
    @Column("start_date")
    val startDate: LocalDate,
    val recurrence: String,

    @Column("target_budget_ids")
    val targetBudgetIds: String,
    @Column("created_date")
    var createdDate: LocalDateTime,
    @Column("updated_date")
    var updatedDate: LocalDateTime,
    @Column("end_date")
    val endDate: LocalDate? = null,
) : JdbcModel() {
    override fun getId(): UUID {
        return spendingPeriodTemplateId
    }
}

@Component
class JdbcSpendingPeriodTemplateMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcSpendingPeriodTemplateModel, SpendingPeriodTemplate> {
    override fun toDomain(model: JdbcSpendingPeriodTemplateModel): SpendingPeriodTemplate {
        val recurrenceJson = objectMapper.readValue<Map<String, Any>>(model.recurrence)

        val budgetIdsSet: Set<UUID> = model.targetBudgetIds?.let { json ->
            objectMapper.readValue<List<String>>(json)
                .map { UUID.fromString(it) }
                .toSet()
        } ?: emptySet()

        val entity = SpendingPeriodTemplate(
            id = model.spendingPeriodTemplateId,
            startDate = model.startDate,
            recurrence = SchedulerRecurrence.fromMap(recurrenceJson),
            isActive = model.isActive,
            targetBudgetIds = budgetIdsSet,
            endDate = model.endDate
        )
        entity.initDate(model.createdDate, model.updatedDate)

        return entity
    }

    override fun toModel(entity: SpendingPeriodTemplate): JdbcSpendingPeriodTemplateModel {
        return JdbcSpendingPeriodTemplateModel(
            spendingPeriodTemplateId = entity.id,
            isActive = entity.isActive,
            startDate = entity.startDate,
            recurrence = objectMapper.writeValueAsString(entity.recurrence.toMap()),
            createdDate = entity.createdAt,
            updatedDate = entity.updatedAt,
            targetBudgetIds = entity.targetBudgetIds.toString(),
            endDate = entity.endDate
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "spending_period_template_id",
        "isActive" to "is_active",
        "startDate" to "start_date",
        "recurrence.period" to "recurrence->>'period'",
        "recurrence.interval" to "recurrence->>'interval'",
        "createdDate" to "created_date",
        "updatedDate" to "updated_date",
        "endDate" to "end_date",
        "targetBudgetIds" to "target_budget_ids"
    )

    override fun getTableName(): String = "spending_period_templates"

    override fun getSortField(): Set<String> {
        return setOf("start_date", "end_date", "created_date", "updated_date")
    }

    override fun getModelClass(): Class<JdbcSpendingPeriodTemplateModel> = JdbcSpendingPeriodTemplateModel::class.java
}