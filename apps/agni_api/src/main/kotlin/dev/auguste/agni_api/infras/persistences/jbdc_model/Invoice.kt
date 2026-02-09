package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("transactions")
data class JdbcInvoiceModel(
    @Id
    @Column("transaction_id")
    val id: UUID,

    @Column("account_id")
    val accountId: UUID,

    val status: String,
    val type: String,
    val mouvement: String,
    val date: LocalDateTime,

    @Column("is_freeze")
    val isFreeze: Boolean,

    val deductions: String
)

@Component
class JdbcInvoiceModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcInvoiceModel, Invoice> {
    override fun toDomain(model: JdbcInvoiceModel): Invoice {
        val deductionsJson = objectMapper.readValue(model.deductions, Array<String>::class.java).map {
            objectMapper.readValue<Map<String, Any>>(it)
        }.toSet()

        return Invoice(
            id = model.id,
            accountId = model.accountId,
            status = InvoiceStatusType.fromString(model.status),
            mouvementType = InvoiceMouvementType.fromString(model.mouvement),
            type = InvoiceType.fromString(model.type),
            deductions = deductionsJson.map { InvoiceDeduction.fromMap(it) }.toMutableSet(),
            date = model.date,
            isFreeze = model.isFreeze,
        )
    }

    override fun toModel(entity: Invoice): JdbcInvoiceModel {
        return JdbcInvoiceModel(
            id = entity.id,
            accountId = entity.accountId,
            status = entity.statusType.value,
            type = entity.type.value,
            mouvement = entity.mouvementType.value,
            date = entity.date,
            isFreeze = entity.isFreeze,
            deductions = objectMapper.writeValueAsString(entity.deductions.map { objectMapper.writeValueAsString(it.toMap()) })
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("date")
    }
}