package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("internal_loans")
data class JdbcInternalLoanModal(
    @Id
    @get:JvmName("getIdentifier")
    @Column("internal_loan_id")
    val id: UUID,
    @Column("credit_target_id")
    val creditTargetId: UUID,
    @Column("invoice_id")
    val invoiceId: UUID,
    @Column("fund_source_id")
    val fundSourceId: UUID,
    @Column("due_date")
    val dueDate: LocalDate) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcInternalLoanMapper: IMapper<JdbcInternalLoanModal, InternalLoan> {
    override fun toDomain(model: JdbcInternalLoanModal): InternalLoan {
        return InternalLoan(
            id = model.id,
            creditTargetId = model.creditTargetId,
            invoiceId = model.invoiceId,
            fundSourceId = model.fundSourceId,
            dueDate = model.dueDate
        )
    }

    override fun toModel(entity: InternalLoan): JdbcInternalLoanModal {
        return JdbcInternalLoanModal(
            id = entity.id,
            creditTargetId = entity.creditTargetId,
            invoiceId = entity.invoiceId,
            fundSourceId = entity.fundSourceId,
            dueDate = entity.dueDate
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("dueDate")
    }
}