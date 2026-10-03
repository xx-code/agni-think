package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import domain.entities.ScheduleInvoice
import domain.enums.InvoiceType
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("schedule_transactions")
data class JdbcScheduleInvoiceModel(
    @Id
    @get:JvmName("getIdentifier")
    val scheduleTransactionId: UUID,

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
    val tagIds: String,

    @Column("end_date")
    val endDate: LocalDateTime?,

    val moduleLinker: String?,
    @Column("freeze_scheduler")
    val freezeScheduler: String?
    ) : JdbcModel() {
    override fun getId(): UUID {
        return scheduleTransactionId
    }
}

@Component
class JdbcScheduleInvoiceMapper(
    private val objectMapper: com.fasterxml.jackson.databind.ObjectMapper
): IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice> {

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

    override fun toDomain(model: JdbcScheduleInvoiceModel): ScheduleInvoice {
        val schedulerJson = jacksonObjectMapper().readValue<Map<String, Any>>(model.scheduler)
        val freezeSchedulerJson = if (
            model.freezeScheduler == "null" || model.freezeScheduler == "[null]" ||
            model.freezeScheduler.isNullOrEmpty() || model.freezeScheduler == "{}" || model.freezeScheduler == "[]"
        ) { null }
        else {  jacksonObjectMapper().readValue<Map<String, Any>>(model.freezeScheduler) }

        val moduleLinkerJson = if (
            model.moduleLinker == "null" || model.moduleLinker == "[null]" ||
            model.moduleLinker.isNullOrEmpty() || model.moduleLinker == "{}" || model.freezeScheduler == "[]"
        ) { null }
        else {  jacksonObjectMapper().readValue<Map<String, Any>>(model.moduleLinker) }


        val tagIdsSet: Set<UUID> = parseUuidSet(model.tagIds)

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
            tagIds =  tagIdsSet.toMutableSet(),
            endDate = model.endDate,
            moduleLinker = moduleLinkerJson.let { ScheduleInvoiceModuleLinker.fromMap(it) },
            freezeScheduler = freezeSchedulerJson?.let {  Scheduler.fromMap(freezeSchedulerJson) },
        )
    }

    override fun toModel(entity: ScheduleInvoice): JdbcScheduleInvoiceModel {
        return JdbcScheduleInvoiceModel(
            scheduleTransactionId = entity.id,
            accountId = entity.accountId,
            categoryId = entity.categoryId,
            amount = entity.amount,
            name = entity.title,
            type = entity.type.value,
            isPause = entity.isPause,
            isFreeze = entity.isFreeze,
            scheduler = objectMapper.writeValueAsString(entity.scheduler.toMap()),
            tagIds = objectMapper.writeValueAsString(entity.tagIds.map { it.toString() }) ,
            endDate = entity.endDate,
            moduleLinker = objectMapper.writeValueAsString(entity.moduleLinker?.toMap()),
            freezeScheduler = objectMapper.writeValueAsString(entity.freezeScheduler?.toMap())
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "schedule_transaction_id",
        "accountId" to "account_id",
        "categoryId" to "category_id",
        "amount" to "amount",
        "title" to "name",
        "type" to "type",
        "isPause" to "is_pause",
        "isFreeze" to "is_freeze",
        "tagIds" to "jsonb_scalar_array:tag_ids",
        "endDate" to "end_date",

        "scheduler.date" to "scheduler->>'due_date'",
        "scheduler.recurrence.period" to "scheduler->>'recurrence'->>'period'",
        "scheduler.recurrence.interval" to "scheduler->>'recurrence'->>'interval'",

        "freezeScheduler.date" to "freeze_scheduler->>'due_date'",
        "freezeScheduler.recurrence.period" to "freeze_scheduler->>'recurrence'->>'period'",
        "freezeScheduler.recurrence.interval" to "freeze_scheduler->>'recurrence'->>'interval'",

        "moduleLinker.sourceId" to "module_linker->>'source_id'",
        "moduleLinker.module" to "module_linker->>'module'",
    )

    override fun getTableName(): String = "schedule_transactions"

    override fun getSortField(): Set<String> {
        return setOf()
    }

    override fun getModelClass(): Class<JdbcScheduleInvoiceModel> = JdbcScheduleInvoiceModel::class.java
}