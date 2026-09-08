package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("schedule_transactions")
data class JdbcScheduleInvoiceModel(
    @Id
    @get:JvmName("getIdentifier")
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
    val tagIds: Set<UUID>,

    @Column("end_date")
    val endDate: LocalDateTime?,

    @Column("freeze_scheduler")
    val freezeScheduler: String?
    ) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcScheduleInvoiceMapper(
    private val objectMapper: com.fasterxml.jackson.databind.ObjectMapper
): IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice> {
    override fun toDomain(model: JdbcScheduleInvoiceModel): ScheduleInvoice {
        val schedulerJson = jacksonObjectMapper().readValue<Map<String, Any>>(model.scheduler)
        val freezeSchedulerJson = if (
            model.freezeScheduler == "null" || model.freezeScheduler == "[null]" ||
            model.freezeScheduler.isNullOrEmpty() || model.freezeScheduler == "{}" || model.freezeScheduler == "[]"
        ) { null }
        else {  jacksonObjectMapper().readValue<Map<String, Any>>(model.freezeScheduler) }

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
            tagIds =  model.tagIds.toMutableSet(),
            endDate = model.endDate,
            freezeScheduler = freezeSchedulerJson?.let {  Scheduler.fromMap(freezeSchedulerJson) },
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
            scheduler = objectMapper.writeValueAsString(entity.scheduler.toMap()),
            tagIds = entity.tagIds,
            endDate = entity.endDate,
            freezeScheduler = objectMapper.writeValueAsString(entity.freezeScheduler?.toMap())
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun getTableName(): String {
        TODO("Not yet implemented")
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }

    override fun getModelClass(): Class<JdbcScheduleInvoiceModel> {
        TODO("Not yet implemented")
    }
}