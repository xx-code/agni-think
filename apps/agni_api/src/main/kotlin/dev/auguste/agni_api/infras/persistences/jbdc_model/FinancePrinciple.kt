package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.entities.enums.PrincipleType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("finance_principles")
data class JdbcFinancePrincipleModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("finance_principle_id")
    val id: UUID,

    val name: String,
    val description: String,
    val targetType: String,
    val strictness: Int,
    val logicRules: String?,

) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcFinancePrincipleMapper: IMapper<JdbcFinancePrincipleModel, FinancePrinciple> {
    override fun toDomain(model: JdbcFinancePrincipleModel): FinancePrinciple {
        return FinancePrinciple(
            id = model.id,
            name = model.name,
            description = model.description,
            strictness = model.strictness,
            targetType = PrincipleType.fromString(model.targetType),
            logicRules = model.logicRules,
        )
    }

    override fun toModel(entity: FinancePrinciple): JdbcFinancePrincipleModel {
        return JdbcFinancePrincipleModel(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            strictness = entity.strictness,
            targetType = entity.targetType.value,
            logicRules = entity.logicRules,
        )
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }
}