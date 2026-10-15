package persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.SpendingPeriod
import domain.enums.SpendingPeriodStateType
import domain.value_objects.SnapshotForcastSpendingPeriod
import domain.value_objects.SpendingPeriodItem
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("spending_periods")
data class JdbcSpendingPeriodModel(
    @Id
    @get:JvmName("getIdentifier")
    val spendingPeriodId: UUID,
    val userId: UUID,
    @Column("spending_period_template_id")
    val spendingPeriodTemplateId: UUID,
    @Column("start_date")
    val startDate: LocalDate,
    @Column("end_date")
    val endDate: LocalDate,

    val closeBalance: Double,

    val savingRateTarget: Double,
    val state: String,
    val forcastSnapshot: String,

    val scheduleIncomesSelected: String,
    val scheduleExpensesSelected: String,
    val budgetsSelected: String,
    val wishlistItemSelected: String,

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
    private fun parseUuidSet(json: String?): Set<UUID> {
        if (json.isNullOrBlank()) return emptySet()

        return runCatching {
            // Lecture du tableau JSON sous forme de List<String>
            objectMapper.readValue<List<String>>(json)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { UUID.fromString(it) }
                .toSet()
        }.getOrElse {
            // Sécurité de secours au cas où la BDD contient des formats mal formés ou entre crochet sans guillemets JSON
            json.trim('[', ']', ' ', '\n', '\r')
                .split(",")
                .map { it.replace("\"", "").trim() }
                .filter { it.isNotEmpty() }
                .map { UUID.fromString(it) }
                .toSet()
        }
    }


    override fun toDomain(model: JdbcSpendingPeriodModel): SpendingPeriod {
        val snapshotJson = objectMapper.readValue<Map<String, Any>>(model.forcastSnapshot)
        val scheduleExpensesSelectedIds: Set<UUID> = parseUuidSet(model.scheduleExpensesSelected)
        val scheduleIncomeSelectedIds: Set<UUID> = parseUuidSet(model.scheduleIncomesSelected)
        val budgetsSelectedIds: Set<UUID> = parseUuidSet(model.budgetsSelected)
        val wishlistItemIds: Set<UUID> = parseUuidSet(model.wishlistItemSelected)

        val entity = SpendingPeriod(
            id = model.spendingPeriodId,
            userId = model.userId,
            spendingPeriodTemplateId = model.spendingPeriodTemplateId,
            startDate = model.startDate,
            endDate = model.endDate,
            closeBalance = model.closeBalance,
            savingRateTarget = model.savingRateTarget,
            state = SpendingPeriodStateType.fromString(model.state),
            snapshot = SnapshotForcastSpendingPeriod.fromMap(snapshotJson),
            scheduleIncomesSelected = scheduleIncomeSelectedIds,
            scheduleExpensesSelected = scheduleExpensesSelectedIds,
            budgetsSelected = budgetsSelectedIds,
            wishlistItemSelected = wishlistItemIds
        )
        entity.initDate(model.createdDate, model.updatedDate)

        return entity
    }

    override fun toModel(entity: SpendingPeriod): JdbcSpendingPeriodModel {
        return JdbcSpendingPeriodModel(
            spendingPeriodId = entity.id,
            userId = entity.userId,
            spendingPeriodTemplateId = entity.spendingPeriodTemplateId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            savingRateTarget = entity.savingRateTarget,
            state = entity.state.value,
            closeBalance = entity.closedBalance,
            createdDate = entity.createdAt,
            updatedDate = entity.updatedAt,
            forcastSnapshot = objectMapper.writeValueAsString(entity.snapshot.toMap()),
            scheduleIncomesSelected = objectMapper.writeValueAsString(entity.scheduleIncomesSelected.map { it.toString() }),
            scheduleExpensesSelected = objectMapper.writeValueAsString(entity.scheduleExpensesSelected.map { it.toString() }),
            budgetsSelected = objectMapper.writeValueAsString(entity.budgetsSelected.map { it.toString() }),
            wishlistItemSelected = objectMapper.writeValueAsString(entity.wishlistItemSelected.map { it.toString() })
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "spending_period_id",
        "userId" to "user_id",
        "spendingPeriodTemplateId" to "spending_period_template_id",
        "closeBalance" to "close_balance",
        "savingRateTarget" to "saving_rate_target",
        "state" to "state",
        "startDate" to "start_date",
        "endDate" to "end_date",

        "scheduleIncomesSelected" to "schedule_incomes_selected",
        "scheduleExpensesSelected" to "schedule_expenses_selected",
        "budgetsSelected" to "budgets_selected",
        "wishlistItemSelected" to "wishlist_item_selected",

        "snapshot.income" to "forcast_snapshot->>'income'",
        "snapshot.fixExpenses" to "forcast_snapshot->>'fixedExpenses'",
        "snapshot.variableExpenses" to "forcast_snapshot->>'variableExpenses'",
        "snapshot.budgetExpenses" to "forcast_snapshot->>'budgetExpenses'",
        "snapshot.saving" to "forcast_snapshot->>'saving'",

        "createdDate" to "created_date",
        "updatedDate" to "updated_date",
    )

    override fun getTableName(): String = "spending_periods"

    override fun getSortField(): Set<String> {
        return setOf("state", "start_date", "end_date", "created_date", "updated_date")
    }

    override fun getModelClass(): Class<JdbcSpendingPeriodModel> = JdbcSpendingPeriodModel::class.java
}
