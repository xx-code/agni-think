package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("patrimonies")
data class JdbcPatrimonyModel(
    @Id
    @Column("patrimony_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val type: String,
    val amount: Double,

    @Column("account_ids")
    val accountIds: Set<UUID>
)

@Component
class JdbcPatrimonyModelMapper: IMapper<JdbcPatrimonyModel, Patrimony> {
    override fun toDomain(model: JdbcPatrimonyModel): Patrimony {
        return Patrimony(
            id = model.id,
            title = model.name,
            amount = model.amount,
            accountIds = model.accountIds.toMutableSet(),
            type = PatrimonyType.fromString(model.type)
        )
    }

    override fun toModel(entity: Patrimony): JdbcPatrimonyModel {
        return JdbcPatrimonyModel(
            id = entity.id,
            name = entity.title,
            amount = entity.amount,
            accountIds = entity.accountIds.toMutableSet(),
            type = entity.type.value,
        )
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }
}