package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction
import dev.auguste.agni_api.core.value_objects.InvoiceModuleLinker
import dev.auguste.agni_api.infras.persistences.IMapper
import org.postgresql.util.PGobject
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("transactions")
data class JdbcInvoiceModel(
    @Id
    @get:JvmName("getIdentifier")
    val transactionId: UUID,

    @Column("account_id")
    val accountId: UUID,

    val status: String,
    val type: String,
    val mouvement: String,
    val date: LocalDateTime,

    @Column("is_freeze")
    val isFreeze: Boolean,

    val deductions: String,
    val invoiceModuleLinkers: String
) : JdbcModel() {
    override fun getId(): UUID {
        return transactionId
    }
}

@Component
class JdbcInvoiceModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcInvoiceModel, Invoice> {
    override fun toDomain(model: JdbcInvoiceModel): Invoice {
        val deductionsJson: Set<Map<String, Any>> = objectMapper.readValue<List<Map<String, Any>>>(model.deductions).toSet()
        val moduleLinkersJson: Set<Map<String, Any>> = objectMapper.readValue<List<Map<String, Any>>>(model.invoiceModuleLinkers).toSet()

        return Invoice(
            id = model.id,
            accountId = model.accountId,
            status = InvoiceStatusType.fromString(model.status),
            mouvementType = InvoiceMouvementType.fromString(model.mouvement),
            type = InvoiceType.fromString(model.type),
            deductions = deductionsJson.map { InvoiceDeduction.fromMap(it) }.toMutableSet(),
            moduleLinkers = moduleLinkersJson.map { InvoiceModuleLinker.fromMap(it) }.toMutableList(),
            date = model.date,
            isFreeze = model.isFreeze,
        )
    }

    override fun toModel(entity: Invoice): JdbcInvoiceModel {
        return JdbcInvoiceModel(
            transactionId = entity.id,
            accountId = entity.accountId,
            status = entity.statusType.value,
            type = entity.type.value,
            mouvement = entity.mouvementType.value,
            date = entity.date,
            isFreeze = entity.isFreeze,
            deductions = objectMapper.writeValueAsString(entity.deductions.map { it.toMap() }),
            invoiceModuleLinkers =  objectMapper.writeValueAsString(entity.moduleLinkers.map { it.toMap() })
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "transaction_id",
        "accountId" to "account_id",
        "statusType" to "status",
        "type" to "type",
        "mouvementType" to "mouvement",
        "date" to "date",
        "isFreeze" to "is_freeze",
        "moduleLinkers.sourceId" to "jsonb_array:invoice_module_linkers->>'source_id'",
        "moduleLinkers.module" to "jsonb_array:invoice_module_linkers->>'module'",
        "deductions.amount" to "jsonb_array:deductions->>'amount'",
        "deductions.deductionId" to "jsonb_array:deductions->>'deduction_id'",
    )

    override fun getTableName(): String = "transactions"

    override fun getSortField(): Set<String> {
        return setOf("date")
    }

    override fun getModelClass(): Class<JdbcInvoiceModel> = JdbcInvoiceModel::class.java
}