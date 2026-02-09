package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("save_goals")
data class JdbcSavingGoalModel(
    @Id
    @Column("save_goal_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val target: Double,
    val balance: Double,

    @Column("desir_value")
    val desirValue: Int,

    @Column("importance")
    val importance: Int,

    @Column("wish_due_date")
    val wishDueDate: LocalDate?,

    val description: String,

    @Column("account_id")
    val accountId: UUID?,

//    @Column("items")
//    val items: String
)

@Component
class JdbcSavingGoalMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcSavingGoalModel, SavingGoal> {
    override fun toDomain(model: JdbcSavingGoalModel): SavingGoal {
        // val itemsJson = jacksonObjectMapper().readValue<Set<Map<String, Any>>>(model.items)

        return SavingGoal(
            id = model.id,
            title = model.name,
            description = model.description,
            target = model.target,
            balance = model.balance,
            desired = IntensityEmotionalDesirType.fromInt(model.desirValue),
            importance = ImportanceGoalType.fromInt(model.importance),
            wishDueDate = model.wishDueDate,
            itemsToTracks = mutableSetOf(), // itemsJson.map { SavingGoalItem.fromMap(it) }.toMutableSet(),
            accountId = model.accountId
        )
    }

    override fun toModel(entity: SavingGoal): JdbcSavingGoalModel {
        return JdbcSavingGoalModel(
            id = entity.id,
            name = entity.title,
            target = entity.target,
            balance = entity.balance,
            desirValue = entity.desired.ordinal,
            importance = entity.importance.ordinal,
            wishDueDate = entity.wishDueDate,
            description = entity.description,
            accountId = entity.accountId,
            // items = jacksonObjectMapper().writeValueAsString(entity.itemsToTracks.map { it.toMap() })
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("wish_due_date", "balance", "target")
    }
}