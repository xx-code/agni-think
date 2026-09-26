package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import dev.auguste.agni_api.core.value_objects.SnapshotForcastSpendingPeriod
import dev.auguste.agni_api.core.value_objects.SpendingPeriodItem
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("spending_periods")
data class JdbcSpendingPeriodModel(
    @Id
    @get:JvmName("getIdentifier")
    val spendingPeriodId: UUID,
    @Column("spending_period_template_id")
    val spendingPeriodTemplateId: UUID,
    @Column("start_date")
    val startDate: LocalDate,
    @Column("end_date")
    val endDate: LocalDate,

    @Column("suggestion_amount")
    val freeAmount: Double,

    val closeBalance: Double,

    val savingRateTarget: Double,
    @Column("total_expected_income")
    val totalExpectedIncome: Double,
    @Column("total_expected_expenses")
    val totalExpectedExpenses: Double,
    val state: String,
    @Column("want_spending_items")
    val wantSpendingItems: String,
    val forcastSnapshot: String,
    @Column("created_date")
    var createdDate: LocalDateTime,
    @Column("updated_date")
    var updatedDate: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return spendingPeriodId
    }
}

@Component
class JdbcSpendingPeriodMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcSpendingPeriodModel, SpendingPeriod> {
    override fun toDomain(model: JdbcSpendingPeriodModel): SpendingPeriod {
        val spendingItemsJson = objectMapper.readValue(model.wantSpendingItems, Array<String>::class.java).map {
            objectMapper.readValue<Map<String, Any>>(it)
        }.toSet()
        val snapshotJson = jacksonObjectMapper().readValue<Map<String, Any>>(model.forcastSnapshot)

        val entity = SpendingPeriod(
            id = model.spendingPeriodId,
            spendingPeriodTemplateId = model.spendingPeriodTemplateId,
            startDate = model.startDate,
            endDate = model.endDate,
            freeAmount = model.freeAmount,
            closeBalance = model.closeBalance,
            savingRateTarget = model.savingRateTarget,
            totalExpectedIncome = model.totalExpectedIncome,
            totalExpectedExpenses = model.totalExpectedExpenses,
            state = SpendingPeriodStateType.fromString(model.state),
            wantSpendingItems = spendingItemsJson.map {
                SpendingPeriodItem.fromMap(it)
            },
            snapshot = SnapshotForcastSpendingPeriod.fromMap(snapshotJson)
        )
        entity.initDate(model.createdDate, model.updatedDate)

        return entity
    }

    override fun toModel(entity: SpendingPeriod): JdbcSpendingPeriodModel {
        return JdbcSpendingPeriodModel(
            spendingPeriodId = entity.id,
            spendingPeriodTemplateId = entity.spendingPeriodTemplateId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            freeAmount = entity.freeAmount,
            savingRateTarget = entity.savingRateTarget,
            totalExpectedIncome = entity.totalExpectedIncome,
            totalExpectedExpenses = entity.totalExpectedExpenses,
            state = entity.state.value,
            closeBalance = entity.closedBalance,
            wantSpendingItems = objectMapper.writeValueAsString(entity.wantSpendingItems),
            createdDate = entity.createdAt,
            updatedDate = entity.updatedAt,
            forcastSnapshot = objectMapper.writeValueAsString(entity.snapshot.toMap())
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "spending_period_id",
        "spendingPeriodTemplateId" to "spending_period_template_id",
        "freeAmount" to "free_amount",
        "closeBalance" to "close_balance",
        "savingRateTarget" to "saving_rate_target",
        "totalExpectedIncome" to "total_expected_income",
        "totalExpectedExpenses" to "total_expected_expenses",
        "state" to "state",
        "startDate" to "start_date",
        "endDate" to "end_date",

        "snapshot.income" to "forcast_snapshot->>'income'",
        "snapshot.fixExpenses" to "forcast_snapshot->>'fixedExpenses'",
        "snapshot.variableExpenses" to "forcast_snapshot->>'variableExpenses'",
        "snapshot.budgetExpenses" to "forcast_snapshot->>'budgetExpenses'",
        "snapshot.saving" to "forcast_snapshot->>'saving'",

        "wantSpendingItems.description" to "want_spending_items->>'description'",
        "wantSpendingItems.amount" to "want_spending_items->>'amount'",
        "createdDate" to "created_date",
        "updatedDate" to "updated_date",
    )

    override fun getTableName(): String = "spending_periods"

    override fun getSortField(): Set<String> {
        return setOf("state", "start_date", "end_date", "created_date", "updated_date")
    }

    override fun getModelClass(): Class<JdbcSpendingPeriodModel> = JdbcSpendingPeriodModel::class.java
}