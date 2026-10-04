package persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.InternalLoan
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDate
import java.util.UUID

@Table("internal_loans")
data class JdbcInternalLoanModal(
    @Id
    @get:JvmName("getIdentifier")
    val internalLoanId: UUID,
    @Column("credit_target_id")
    val creditTargetId: UUID,
    @Column("invoice_id")
    val invoiceId: UUID,
    @Column("fund_source_id")
    val fundSourceId: UUID,
    @Column("due_date")
    val dueDate: LocalDate,
    @Column("refund_ids")
    val refundIds: String
) : JdbcModel() {
    override fun getId(): UUID {
        return internalLoanId
    }
}

@Component
class JdbcInternalLoanMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcInternalLoanModal, InternalLoan> {
    // TODO: Refactor to make the more resiliant an not duplacte
    private fun parseUuidSet(json: String?): Set<UUID> {
        if (json.isNullOrBlank()) return emptySet()

        return runCatching {
            // Lecture du tableau JSON sous forme de List<String>
            objectMapper.readValue<List<String>>(json)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { UUID.fromString(it) }
                .toSet()
        }.getOrElse {
            // Sécurité de secours au cas où la BDD contient des formats mal formés ou entre crochet sans guillemets JSON
            json.trim('[', ']', ' ', '\n', '\r')
                .split(",")
                .map { it.replace("\"", "").trim() }
                .filter { it.isNotEmpty() }
                .map { UUID.fromString(it) }
                .toSet()
        }
    }

    override fun toDomain(model: JdbcInternalLoanModal): InternalLoan {
        val trackFundIds: Set<UUID> = parseUuidSet(model.refundIds)

        return InternalLoan(
            id = model.id,
            creditTargetId = model.creditTargetId,
            invoiceId = model.invoiceId,
            fundSourceId = model.fundSourceId,
            dueDate = model.dueDate,
            trackRefunds = trackFundIds
        )
    }

    override fun toModel(entity: InternalLoan): JdbcInternalLoanModal {
        return JdbcInternalLoanModal(
            internalLoanId = entity.id,
            creditTargetId = entity.creditTargetId,
            invoiceId = entity.invoiceId,
            fundSourceId = entity.fundSourceId,
            dueDate = entity.dueDate,
            refundIds = objectMapper.writeValueAsString(entity.trackRefunds.map { it.toString() })
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "internal_loan_id",
        "creditTargetId" to "credit_target_id",
        "invoiceId" to "invoice_id",
        "fundSourceId" to "fund_source_id",
        "dueDate" to "due_date",
        "trackRefunds" to "jsonb_scalar_array:refund_ids"
    )

    override fun getTableName(): String = "internal_loans"

    override fun getSortField(): Set<String> {
        return setOf("due_date")
    }

    override fun getModelClass(): Class<JdbcInternalLoanModal> = JdbcInternalLoanModal::class.java
}
