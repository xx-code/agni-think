package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.infras.persistences.IMapper
import kotlinx.serialization.json.Json
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Table("schedule_transactions")
data class JdbcScheduleInvoiceModel(
    @Id
    @Column("schedule_transaction_id")
    val id: UUID,

    @Column("account_id")
    val accountId: UUID,

    @Column("category_id")
    val categoryId: UUID,

    val amount: Double,
    val name: String,
    val type: String,

    @Column("is_pause")
    val isPause: Boolean,

    @Column("is_freeze")
    val isFreeze: Boolean,

    val scheduler: String,

    @Column("tag_ids")
    val tagIds: Set<UUID>
)

@Component
class JdbcScheduleInvoiceMapper: IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice> {
    override fun toDomain(model: JdbcScheduleInvoiceModel): ScheduleInvoice {
        val schedulerJson = jacksonObjectMapper().readValue<Map<String, Any>>(model.scheduler)

        return ScheduleInvoice(
            id = model.id,
            title = model.name,
            accountId = model.accountId,
            type = InvoiceType.fromString(model.type),
            amount = model.amount,
            scheduler = Scheduler.fromMap(schedulerJson),
            categoryId = model.categoryId,
            isPause = model.isPause,
            isFreeze = model.isFreeze,
            tagIds =  model.tagIds.toMutableSet()
        )
    }

    override fun toModel(entity: ScheduleInvoice): JdbcScheduleInvoiceModel {
        return JdbcScheduleInvoiceModel(
            id = entity.id,
            accountId = entity.accountId,
            categoryId = entity.categoryId,
            amount = entity.amount,
            name = entity.title,
            type = entity.type.value,
            isPause = entity.isPause,
            isFreeze = entity.isFreeze,
            scheduler = jacksonObjectMapper().writeValueAsString(entity.scheduler.toMap()),
            tagIds = entity.tagIds
        )
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }
}