package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Provisionable
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("provisionables")
data class JdbcProvisionableModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("provisionable_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val initialCost: Double,

    @Column("acquisition_date")
    val acquisitionDate: LocalDate,

    @Column("expected_lifespan_month")
    val expectedLifespanMonth: Int,

    val residualValue: Double,
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcProvisionableMapper: IMapper<JdbcProvisionableModel, Provisionable> {
    override fun toDomain(model: JdbcProvisionableModel): Provisionable {
        return Provisionable(
            id = model.id,
            title = model.name,
            initialCost = model.initialCost,
            acquisitionDate = model.acquisitionDate,
            expectedLifespanMonth = model.expectedLifespanMonth,
            residualValue = model.residualValue
        )
    }

    override fun toModel(entity: Provisionable): JdbcProvisionableModel {
        return JdbcProvisionableModel(
            id = entity.id,
            name = entity.title,
            initialCost = entity.initialCost,
            acquisitionDate = entity.acquisitionDate,
            expectedLifespanMonth = entity.expectedLifespanMonth,
            residualValue = entity.residualValue
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("acquisition_date")
    }
}