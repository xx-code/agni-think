package persistences.jbdc_model

import domain.entities.ExternalTransaction
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDateTime
import java.util.UUID

@Table("external_transactions")
data class JdbcExternalTransactionModel(
    @Id
    @get:JvmName("getIdentifier")
    val externalTransactionId: UUID,
    @Column("transaction_id")
    val transactionId: String,
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
        return externalTransactionId
    }
}

@Component
class JdbcExternalTransactionModelMapper: IMapper<JdbcExternalTransactionModel, ExternalTransaction> {
    override fun toDomain(model: JdbcExternalTransactionModel): ExternalTransaction {
        return ExternalTransaction(
            id = model.id,
            transactionId = model.transactionId,
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
            externalTransactionId = entity.id,
            accountId = entity.accountId,
            transactionId = entity.transactionId,
            amount = entity.amount,
            merchantName = entity.merchantName,
            categoryPrimary = entity.categoryPrimary,
            categoryDetail = entity.categoryDetail,
            isTreated = entity.isTreated,
            dateTransaction = entity.dateTransaction,
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "external_transaction_id",
        "transactionId" to "transaction_id",
        "accountId" to "account_id",
        "amount" to "amount",
        "dateTransaction" to "date_transaction",
        "merchantName" to "merchant_name",
        "categoryPrimary" to "category_primary",
        "categoryDetail" to "category_detail",
        "isTreated" to "is_treated"
    )

    override fun getTableName(): String = "external_transactions"

    override fun getSortField(): Set<String> {
        return setOf()
    }

    override fun getModelClass(): Class<JdbcExternalTransactionModel> = JdbcExternalTransactionModel::class.java
}