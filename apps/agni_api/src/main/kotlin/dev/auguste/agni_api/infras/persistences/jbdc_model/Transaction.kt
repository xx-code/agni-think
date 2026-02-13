package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("records")
data class JdbcTransactionModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("record_id")
    val id: UUID,

    @Column("transaction_id")
    val invoiceId: UUID,

    @Column("money_amount")
    val amount: Double,

    @Column("category_id")
    val categoryId: UUID,

    val description: String,

    @Column("tag_ids")
    val tagIds: Set<UUID>,

    @Column("budget_ids")
    val budgetIds: Set<UUID>
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcTransactionModelMapper: IMapper<JdbcTransactionModel, Transaction> {
    override fun toDomain(model: JdbcTransactionModel): Transaction {
        return Transaction(
            id = model.id,
            invoiceId = model.invoiceId,
            categoryId = model.categoryId,
            amount = model.amount,
            tagIds = model.tagIds.toMutableSet(),
            budgetIds = model.budgetIds.toMutableSet(),
            description = model.description
        )
    }

    override fun toModel(entity: Transaction): JdbcTransactionModel {
        return  JdbcTransactionModel(
            id = entity.id,
            invoiceId = entity.invoiceId,
            amount = entity.amount,
            categoryId = entity.categoryId,
            description = entity.description,
            tagIds = entity.tagIds.toMutableSet(),
            budgetIds = entity.budgetIds.toMutableSet()
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("money_amount", "category_id")
    }
}