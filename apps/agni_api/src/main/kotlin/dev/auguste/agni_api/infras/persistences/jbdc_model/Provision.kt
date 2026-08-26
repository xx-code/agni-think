package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.DomainException
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
import java.util.UUID

@Table("provisions")
data class JdbcProvisionModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("provision_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val initialCost: Double,

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

    val type: String,

    @Column("depreciate_criteria")
    val depreciateCriteria: String,

    @Column("payment_info")
    val paymentInfo: String?

) : JdbcModel() {
    override fun getId(): UUID {
        return id
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

        return Provision(
            id = model.id,
            title = model.name,
            initialCost = model.initialCost,
            acquisitionDate = model.acquisitionDate,
            expectedLifespanMonth = model.expectedLifespanMonth,
            isPatrimony = model.isPatrimony,
            depreciationCriteria = depreciateCriteriaJson.map { ProvisionDepreciateCriteria.fromMap(it) }.toMutableList(),
            floorValue = model.floorValue,
            type = ProvisionType.fromString(model.type),
            paymentInfo = model.paymentInfo?.let { ProvisionPayment.fromMap(objectMapper.readValue<Map<String, Any>>(it)) } ,
            interestLoan = model.interestLoan,
            loanMonth = model.loanMonth,
        )
    }

    override fun toModel(entity: Provision): JdbcProvisionModel {
        return JdbcProvisionModel(
            id = entity.id,
            name = entity.title,
            initialCost = entity.initialCost,
            acquisitionDate = entity.acquisitionDate,
            expectedLifespanMonth = entity.expectedLifespanMonth,
            isPatrimony = entity.isPatrimony,
            interestLoan = entity.interestLoan,
            loanMonth = entity.loanMonth,
            floorValue = entity.floorValue,
            type = entity.type.value,
            depreciateCriteria = objectMapper.writeValueAsString(entity.depreciationCriteria.map { objectMapper.writeValueAsString(it.toMap()) }),
            paymentInfo = entity.paymentInfo?.let { objectMapper.writeValueAsString(it.toMap()) }
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("acquisition_date")
    }
}