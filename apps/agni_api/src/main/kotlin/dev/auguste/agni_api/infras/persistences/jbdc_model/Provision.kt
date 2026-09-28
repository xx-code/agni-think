package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("provisions")
data class JdbcProvisionModel(
    @Id
    @get:JvmName("getIdentifier")
    val provisionId: UUID,

    val title: String,

    @Column("cost_ht")
    val costHt: Double,

    @Column("cost_ttc")
    val costTtc: Double,

    @Column("acquisition_date")
    val acquisitionDate: LocalDate,

    @Column("expected_lifespan_month")
    val expectedLifespanMonth: Int,

    @Column("is_patrimony")
    val isPatrimony: Boolean,

    @Column("interest_loan")
    val interestLoan: Double,

    @Column("loan_month")
    val loanMonth: Long,

    @Column("floor_value")
    val floorValue: Double,

    @Column("provision_type")
    val provisionType: String,

    @Column("depreciate_criteria")
    val depreciateCriteria: String,

    val isInstallmentOnTtc: Boolean,

    @Column("fund_amortization_id")
    val fundAmortizationId: UUID?,

    @Column("payment_info")
    val paymentInfo: String?,

    @Column("created_at")
    val createdAt: LocalDateTime,

    @Column("updated_at")
    val updatedAt: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return provisionId
    }
}

@Component
class JdbcProvisionMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcProvisionModel, Provision> {

    override fun toDomain(model: JdbcProvisionModel): Provision {
        val depreciateCriteriaJson = objectMapper.readValue(model.depreciateCriteria, Array<String>::class.java).map {
            objectMapper.readValue<Map<String, Any>>(it)
        }.toSet()

        val paymentInfoJson = if (
            model.paymentInfo == "null" || model.paymentInfo == "[null]" ||
            model.paymentInfo.isNullOrEmpty() || model.paymentInfo == "{}" || model.paymentInfo == "[]"
        ) { null }
        else { jacksonObjectMapper().readValue<Map<String, Any>>(model.paymentInfo) }

        return Provision(
            id = model.id,
            title = model.title,
            costHT = model.costHt,
            costTTC = model.costTtc,
            acquisitionDate = model.acquisitionDate,
            expectedLifespanMonth = model.expectedLifespanMonth,
            isPatrimony = model.isPatrimony,
            depreciationCriteria = depreciateCriteriaJson.map { ProvisionDepreciateCriteria.fromMap(it) }.toMutableList(),
            floorValue = model.floorValue,
            isInstallmentOnTTC = model.isInstallmentOnTtc,
            fundAmortizationId = model.fundAmortizationId,
            type = ProvisionType.fromString(model.provisionType),
            paymentInfo = paymentInfoJson?.let { ProvisionPayment.fromMap(it) },
            interestLoan = model.interestLoan,
            loanMonth = model.loanMonth,
        )
    }

    override fun toModel(entity: Provision): JdbcProvisionModel {
        return JdbcProvisionModel(
            provisionId = entity.id,
            title = entity.title,
            costHt = entity.costHT,
            costTtc = entity.costTTC,
            acquisitionDate = entity.acquisitionDate,
            expectedLifespanMonth = entity.expectedLifespanMonth,
            isPatrimony = entity.isPatrimony,
            interestLoan = entity.interestLoan,
            loanMonth = entity.loanMonth,
            floorValue = entity.floorValue,
            provisionType = entity.type.value,
            isInstallmentOnTtc = entity.isInstallmentOnTTC,
            fundAmortizationId = entity.fundAmortizationId,
            depreciateCriteria = objectMapper.writeValueAsString(entity.depreciationCriteria.map { objectMapper.writeValueAsString(it.toMap()) }),
            paymentInfo = if (entity.paymentInfo != null) {
                objectMapper.writeValueAsString(entity.paymentInfo!!.toMap())
            } else { "null" },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mutableMapOf(
        "id" to "provision_id",
        "title" to "title",
        "costHT" to "cost_ht",
        "costTTC" to "cost_ttc",
        "acquisitionDate" to "acquisition_date",
        "expectedLifespanMonth" to "expected_lifespan_month",
        "isPatrimony" to "is_patrimony",
        "interestLoan" to "interest_loan",
        "loanMonth" to "loan_month",
        "floorValue" to "floor_value",
        "type" to "provision_type",
        "fundAmortizationId" to "fund_amortization_id",
        "isInstallmentOnTTC" to "is_installment_on_ttc",

        // Critères de dépréciation
        "depreciationCriteria.title" to "depreciate_criteria->>'title'",
        "depreciationCriteria.description" to "depreciate_criteria->>'description'",
        "depreciationCriteria.type" to "depreciate_criteria->>'type'",
        "depreciationCriteria.value" to "depreciate_criteria->>'value'",
        "depreciationCriteria.monthRange" to "depreciate_criteria->>'monthRange'",

        // Informations de paiement
        "paymentInfo.accountId" to "payment_info->>'account_id'",
        "paymentInfo.categoryId" to "payment_info->>'category_id'",
        "paymentInfo.budgetIds" to "payment_info->>'budget_ids'",
        "paymentInfo.tagIds" to "payment_info->>'tag_ids'",
        "paymentInfo.paymentAmount" to "payment_info->>'payment_amount'",
        "paymentInfo.endDate" to "payment_info->>'end_date'",

        // Syntaxe JSONB PostgreSQL corrigée (remplacement de ->> imbriqué par -> puis ->>)
        "paymentInfo.scheduler.date" to "payment_info->'scheduler'->>'due_date'",
        "paymentInfo.scheduler.recurrence.period" to "payment_info->'scheduler'->'recurrence'->>'period'",
        "paymentInfo.scheduler.recurrence.interval" to "payment_info->'scheduler'->'recurrence'->>'interval'",

        "createdAt" to "created_at",
        "updatedAt" to "updated_at",
    )

    override fun getTableName(): String = "provisions"

    override fun getSortField(): Set<String> {
        return setOf("acquisition_date", "updated_at")
    }

    override fun getModelClass(): Class<JdbcProvisionModel> = JdbcProvisionModel::class.java
}