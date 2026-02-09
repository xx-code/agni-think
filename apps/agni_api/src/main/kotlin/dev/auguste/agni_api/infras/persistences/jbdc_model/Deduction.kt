package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("deduction_types")
data class JdbcDeductionModel(
    @Id
    @Column("deduction_type_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val description: String,
    val base: String,
    val mode: String
)

@Component
class JdbcDeductionModelMapper: IMapper<JdbcDeductionModel, Deduction> {
    override fun toDomain(model: JdbcDeductionModel): Deduction {
        return Deduction(
            id = model.id,
            title = model.name,
            base = DeductionBaseType.fromString(model.base),
            mode = DeductionModeType.fromString(model.mode),
            description = model.description
        )
    }

    override fun toModel(entity: Deduction): JdbcDeductionModel {
        return JdbcDeductionModel(
            id = entity.id,
            name = entity.title,
            description = entity.description,
            base = entity.base.value,
            mode = entity.mode.value
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("rate_to_base")
    }
}