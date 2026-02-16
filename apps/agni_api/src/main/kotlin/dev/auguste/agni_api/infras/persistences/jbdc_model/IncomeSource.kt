package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("income_sources")
data class JdbcIncomeSourceModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("income_source_id")
    val id: UUID,

    val name: String,
    val type: String,
    val payFrequency: String,
    val reliabilityLevel: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val taxRate: Double,
    val otherRate: Double,
    val linkedAccountId: UUID?,
    val annualGrossAmount: Double?) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcIncomeSourceMapper: IMapper<JdbcIncomeSourceModel, IncomeSource> {
    override fun toDomain(model: JdbcIncomeSourceModel): IncomeSource {
        return IncomeSource(
            id = model.id,
            title = model.name,
            type = IncomeSourceType.fromString(model.type),
            payFrequency = IncomeSourceFrequencyType.fromString(model.payFrequency),
            reliabilityLevel = model.reliabilityLevel,
            startDate = model.startDate,
            taxRate = model.taxRate,
            otherRate = model.otherRate,
            linkedAccountId = model.linkedAccountId,
            annualGrossAmount = model.annualGrossAmount,
            endDate = model.endDate
        )
    }

    override fun toModel(entity: IncomeSource): JdbcIncomeSourceModel {
        return JdbcIncomeSourceModel(
            id = entity.id,
            name = entity.title,
            type = entity.type.value,
            payFrequency = entity.payFrequency.value,
            reliabilityLevel = entity.reliabilityLevel,
            startDate = entity.startDate,
            taxRate = entity.taxRate,
            otherRate = entity.otherRate,
            linkedAccountId = entity.linkedAccountId,
            annualGrossAmount = entity.annualGrossAmount,
            endDate = entity.endDate
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("startDate", "endDate", "taxRate", "otherRate")
    }
}