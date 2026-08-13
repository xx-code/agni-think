package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.entities.SavingGoal
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
    @Column("fund_id")
    val id: UUID,
    @Column("title")
    val name: String,
    val target: Double,
    val balance: Double,
    val description: String,
    @Column("account_id")
    val accountId: UUID?,
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
class JdbcSavingGoalMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcSavingGoalModel, SavingGoal> {
    override fun toDomain(model: JdbcSavingGoalModel): SavingGoal {
        return SavingGoal(
            id = model.id,
            title = model.name,
            description = model.description,
            target = model.target,
            balance = model.balance,
            accountId = model.accountId
        )
    }

    override fun toModel(entity: SavingGoal): JdbcSavingGoalModel {
        return JdbcSavingGoalModel(
            id = entity.id,
            name = entity.title,
            target = entity.target,
            balance = entity.balance,
            description = entity.description,
            accountId = entity.accountId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("balance", "target", "created_at", "updated_at")
    }
}