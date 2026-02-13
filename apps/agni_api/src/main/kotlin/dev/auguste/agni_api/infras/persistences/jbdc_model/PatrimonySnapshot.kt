package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
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
    @Column("patrimony_snapshot_id")
    val id: UUID,

    @Column("patrimony_id")
    val patrimonyId: UUID,

    val balance: Double,
    val date: LocalDate,
    val status: String
) : JdbcModel() {
    override fun getId(): UUID {
        return id
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
            id = entity.id,
            patrimonyId = entity.patrimonyId,
            date = entity.date,
            balance = entity.currentBalanceObserved,
            status = entity.status.value,
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("date")
    }
}