package dev.auguste.agni_api.infras.persistences.jbdc_model

import domain.entities.PatrimonySnapshot
import domain.enums.PatrimonySnapshotStatusType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("patrimony_snapshots")
data class JdbcPatrimonySnapshotModel(
    @Id
    @get:JvmName("getIdentifier")
    val patrimonySnapshotId: UUID,

    @Column("patrimony_id")
    val patrimonyId: UUID,

    val balance: Double,
    val date: LocalDate,
    val status: String
) : JdbcModel() {
    override fun getId(): UUID {
        return patrimonySnapshotId
    }
}

@Component
class JdbcPatrimonySnapshotMapper: IMapper<JdbcPatrimonySnapshotModel, PatrimonySnapshot> {
    override fun toDomain(model: JdbcPatrimonySnapshotModel): PatrimonySnapshot {
        return PatrimonySnapshot(
            id = model.id,
            patrimonyId = model.patrimonyId ,
            date = model.date,
            currentBalanceObserved = model.balance,
            status = PatrimonySnapshotStatusType.fromString(model.status),
        )
    }

    override fun toModel(entity: PatrimonySnapshot): JdbcPatrimonySnapshotModel {
        return JdbcPatrimonySnapshotModel(
            patrimonySnapshotId = entity.id,
            patrimonyId = entity.patrimonyId,
            date = entity.date,
            balance = entity.currentBalanceObserved,
            status = entity.status.value,
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "patrimony_snapshot_id",
        "patrimonyId" to "patrimony_id",
        "date" to "date",
        "currentBalanceObserved" to "balance",
        "status" to "status"
    )

    override fun getTableName(): String = "patrimony_snapshots"

    override fun getSortField(): Set<String> {
        return setOf("date")
    }

    override fun getModelClass(): Class<JdbcPatrimonySnapshotModel> = JdbcPatrimonySnapshotModel::class.java
}