package persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.Fund
import domain.enums.FundType
import domain.value_objects.SinkingFundDetail
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDateTime
import java.util.UUID

@Table("funds")
data class JdbcFundModel(
    @Id
    @get:JvmName("getIdentifier")
    val fundId: UUID,
    val userId: UUID,
    val title: String,
    val target: Double,
    val balance: Double,
    val description: String,
    val type: String,
    @Column("account_id")
    val accountId: UUID?,

    @Column("sinking_fund_detail")
    val sinkingFundDetail: String?,

    @Column("created_at")
    val createdAt: LocalDateTime,
    @Column("updated_at")
    val updatedAt: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return fundId
    }
}


@Component
class JdbcFundMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcFundModel, Fund> {
    override fun toDomain(model: JdbcFundModel): Fund {
        val sinkingFundDetailJson = if (
            model.sinkingFundDetail == "null" || model.sinkingFundDetail == "[null]" ||
            model.sinkingFundDetail.isNullOrEmpty() || model.sinkingFundDetail == "{}" || model.sinkingFundDetail == "[]"
        ) { null }
        else { objectMapper.readValue<Map<String, Any>>(model.sinkingFundDetail) }

        return Fund(
            id = model.fundId,
            userId = model.userId,
            title = model.title,
            description = model.description,
            target = model.target,
            balance = model.balance,
            type = FundType.fromString(model.type),
            accountId = model.accountId,
            sinkingFundDetail = sinkingFundDetailJson?.let { SinkingFundDetail.fromMap(it) }
        )
    }

    override fun toModel(entity: Fund): JdbcFundModel {
        return JdbcFundModel(
            fundId = entity.id,
            userId = entity.userId,
            title = entity.title,
            target = entity.target,
            balance = entity.balance,
            description = entity.description,
            accountId = entity.accountId,
            type = entity.type.value,
            sinkingFundDetail = objectMapper.writeValueAsString(entity.sinkingFundDetail?.toMap()),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "fund_id",
        "userId" to "user_id",
        "title" to "title",
        "target" to "target",
        "balance" to "balance",
        "description" to "description",
        "accountId" to "account_id",
        "type" to "type",

        "sinkingFundDetail.startDate" to "sinking_fund_detail->>'start_date'",
        "sinkingFundDetail.numberOfContributions" to "sinking_fund_detail->>'number_of_contributions'",

        "sinkingFundDetail.contributionRecurrence" to "sinking_fund_detail->>'contribution_recurrence'",
        "sinkingFundDetail.contributionRecurrence.period" to "sinking_fund_detail->>'contribution_recurrence'->>'period'",
        "sinkingFundDetail.contributionRecurrence.interval" to "sinking_fund_detail->>'contribution_recurrence'->>'interval'",

        "sinkingFundDetail.fundRecurrence" to "sinking_fund_detail->>'fund_recurrence'",
        "sinkingFundDetail.fundRecurrence.date" to "sinking_fund_detail->>'fund_recurrence'->>'due_date'",
        "sinkingFundDetail.fundRecurrence.repeater" to "sinking_fund_detail->>'fund_recurrence'->>'repeater'",
        "sinkingFundDetail.fundRecurrence.repeater.period" to "sinking_fund_detail->>'fund_recurrence'->>'repeater'->>'period'",
        "sinkingFundDetail.fundRecurrence.repeater.interval" to "sinking_fund_detail->>'fund_recurrence'->>'repeater'->>'interval'",

        "createdAt" to "created_at",
        "updatedAt" to "updated_at"
    )

    override fun getTableName(): String = "funds"

    override fun getSortField(): Set<String> {
        return setOf("balance", "target", "created_at", "updated_at")
    }

    override fun getModelClass(): Class<JdbcFundModel> = JdbcFundModel::class.java
}