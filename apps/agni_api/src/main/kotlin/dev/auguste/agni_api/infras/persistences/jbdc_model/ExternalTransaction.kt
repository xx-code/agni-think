package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("external_transactions")
data class JdbcExternalTransactionModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("external_transaction_id")
    val id: UUID,
    @Column("account_id")
    val accountId: String,
    val amount: Double,
    @Column("merchant_name")
    val merchantName: String,
    @Column("category_primary")
    val categoryPrimary: String,
    @Column("category_detail")
    val categoryDetail: String,
    @Column("is_treated")
    val isTreated: Boolean,
    @Column("date_transaction")
    val dateTransaction: LocalDateTime,
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcExternalTransactionModelMapper: IMapper<JdbcExternalTransactionModel, ExternalTransaction> {
    override fun toDomain(model: JdbcExternalTransactionModel): ExternalTransaction {
        return ExternalTransaction(
            id = model.id,
            accountId = model.accountId,
            amount = model.amount,
            dateTransaction = model.dateTransaction,
            merchantName = model.merchantName,
            categoryPrimary = model.categoryPrimary,
            categoryDetail = model.categoryDetail,
            isTreated = model.isTreated
        )
    }

    override fun toModel(entity: ExternalTransaction): JdbcExternalTransactionModel {
        return JdbcExternalTransactionModel(
            id = entity.id,
            accountId = entity.accountId,
            amount = entity.amount,
            merchantName = entity.merchantName,
            categoryPrimary = entity.categoryPrimary,
            categoryDetail = entity.categoryDetail,
            isTreated = entity.isTreated,
            dateTransaction = entity.dateTransaction,
        )
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }
}