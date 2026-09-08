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

    override fun toDomain(model: JdbcTransactionModel): Transaction {
        val budgetIdsSet: Set<UUID> = parseUuidSet(model.budgetIds)
        val tagIdsSet: Set<UUID> = parseUuidSet(model.tagIds)

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
            tagIds = objectMapper.writeValueAsString(entity.tagIds.map { it.toString() }) ,
            budgetIds = objectMapper.writeValueAsString(entity.budgetIds.map {it.toString()})
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