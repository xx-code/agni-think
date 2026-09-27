package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.FundType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Table("funds")
data class JdbcSavingGoalModel(
    @Id
    @get:JvmName("getIdentifier")
    val fundId: UUID,
    val title: String,
    val target: Double,
    val balance: Double,
    val description: String,
    val type: String,
    @Column("account_id")
    val accountId: UUID?,
    @Column("created_at")
    val createdAt: LocalDateTime,
    @Column("updated_at")
    val updatedAt: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return fundId
    }
}

@Component
class JdbcSavingGoalMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcSavingGoalModel, SavingGoal> {
    override fun toDomain(model: JdbcSavingGoalModel): SavingGoal {
        return SavingGoal(
            id = model.id,
            title = model.title,
            description = model.description,
            target = model.target,
            balance = model.balance,
            type = FundType.fromString(model.type),
            accountId = model.accountId
        )
    }

    override fun toModel(entity: SavingGoal): JdbcSavingGoalModel {
        return JdbcSavingGoalModel(
            fundId = entity.id,
            title = entity.title,
            target = entity.target,
            balance = entity.balance,
            description = entity.description,
            accountId = entity.accountId,
            type = entity.type.value,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "fund_id",
        "title" to "title",
        "target" to "target",
        "balance" to "balance",
        "description" to "description",
        "accountId" to "account_id",
        "type" to "type",
        "createdAt" to "created_at",
        "updatedAt" to "updated_at"
    )

    override fun getTableName(): String = "funds"

    override fun getSortField(): Set<String> {
        return setOf("balance", "target", "created_at", "updated_at")
    }

    override fun getModelClass(): Class<JdbcSavingGoalModel> = JdbcSavingGoalModel::class.java
}