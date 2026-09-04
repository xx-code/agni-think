package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
    val recordId: UUID,

    @Column("transaction_id")
    val transactionId: UUID,

    val moneyAmount: Double,

    @Column("category_id")
    val categoryId: UUID,

    val description: String,

    @Column("tag_ids")
    val tagIds: String,

    @Column("budget_ids")
    val budgetIds: String
) : JdbcModel() {
    override fun getId(): UUID {
        return recordId
    }
}

@Component
class JdbcTransactionModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcTransactionModel, Transaction> {
    override fun toDomain(model: JdbcTransactionModel): Transaction {
        val budgetIdsSet: Set<UUID> = model.budgetIds?.let { json ->
            objectMapper.readValue<List<String>>(json)
                .map { UUID.fromString(it) }
                .toSet()
        } ?: emptySet()
        val tagIdsSet: Set<UUID> = model.tagIds?.let { json ->
            objectMapper.readValue<List<String>>(json)
                .map { UUID.fromString(it) }
                .toSet()
        } ?: emptySet()

        return Transaction(
            id = model.transactionId,
            invoiceId = model.transactionId,
            categoryId = model.categoryId,
            amount = model.moneyAmount,
            tagIds = tagIdsSet.toMutableSet(),
            budgetIds = budgetIdsSet.toMutableSet(),
            description = model.description
        )
    }

    override fun toModel(entity: Transaction): JdbcTransactionModel {
        return  JdbcTransactionModel(
            recordId = entity.id,
            transactionId = entity.invoiceId,
            moneyAmount = entity.amount,
            categoryId = entity.categoryId,
            description = entity.description,
            tagIds = entity.tagIds.toString(),
            budgetIds = entity.budgetIds.toString()
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "record_id",
        "invoiceId" to "transaction_id",
        "categoryId" to "category_id",
        "amount" to "money_amount",
        "tagIds" to "tag_ids",
        "budgetIds" to "budget_ids",
        "description" to "description"
    )

    override fun getTableName(): String = "records"

    override fun getSortField(): Set<String> {
        return setOf("money_amount", "category_id")
    }

    override fun getModelClass(): Class<JdbcTransactionModel> = JdbcTransactionModel::class.java
}