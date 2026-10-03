package dev.auguste.agni_api.infras.persistences.jbdc_model

import domain.entities.IncomeSource
import domain.enums.IncomeSourceFrequencyType
import domain.enums.IncomeSourceType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("income_sources")
data class JdbcIncomeSourceModel(
    @Id
    @get:JvmName("getIdentifier")
    val incomeSourceId: UUID,

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
        return incomeSourceId
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
            incomeSourceId = entity.id,
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

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "income_source_id",
        "title" to "name",
        "type" to "type",
        "payFrequency" to "pay_frequency",
        "reliabilityLevel" to "reliability_level",
        "startDate" to "start_date",
        "endDate" to "end_date",
        "taxRate" to "tax_rate",
        "otherRate" to "other_rate",
        "linkedAccountId" to "linked_account_id",
        "annualGrossAmount" to "annual_gross_amount"
    )

    override fun getTableName(): String = "income_sources"

    override fun getSortField(): Set<String> {
        return setOf("startDate", "endDate", "taxRate", "otherRate")
    }

    override fun getModelClass(): Class<JdbcIncomeSourceModel> = JdbcIncomeSourceModel::class.java
}