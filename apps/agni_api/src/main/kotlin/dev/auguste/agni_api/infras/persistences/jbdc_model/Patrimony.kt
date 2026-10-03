package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.Patrimony
import domain.enums.PatrimonyType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("patrimonies")
data class JdbcPatrimonyModel(
    @Id
    @get:JvmName("getIdentifier")
    val patrimonyId: UUID,

    val title: String,

    val type: String,
    val amount: Double,

    @Column("account_ids")
    val accountIds: String
) : JdbcModel() {
    override fun getId(): UUID {
        return patrimonyId
    }
}

@Component
class JdbcPatrimonyModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcPatrimonyModel, Patrimony> {
    override fun toDomain(model: JdbcPatrimonyModel): Patrimony {
        val accountIds = objectMapper.readValue<List<String>>(model.accountIds)
            .map { UUID.fromString(it) }
            .toSet()

        return Patrimony(
            id = model.id,
            title = model.title,
            amount = model.amount,
            accountIds = accountIds.toMutableSet(),
            type = PatrimonyType.fromString(model.type)
        )
    }

    override fun toModel(entity: Patrimony): JdbcPatrimonyModel {
        return JdbcPatrimonyModel(
            patrimonyId = entity.id,
            title = entity.title,
            amount = entity.amount,
            accountIds = objectMapper.writeValueAsString(entity.accountIds.map { it.toString() }),
            type = entity.type.value,
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "patrimony_id",
        "title" to "title",
        "type" to "type",
        "amount" to "amount",
        "accountIds" to "jsonb_scalar_array:account_ids"
    )

    override fun getTableName(): String = "patrimonies"

    override fun getSortField(): Set<String> {
        return setOf()
    }

    override fun getModelClass(): Class<JdbcPatrimonyModel> = JdbcPatrimonyModel::class.java
}