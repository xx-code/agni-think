package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Table("finance_reports")
data class JdbcFinanceReportModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("finance_report_id")
    val id: UUID,
    val title: String,
    val description: String,
    val date: LocalDate
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcFinanceReportModelMapper: IMapper<JdbcFinanceReportModel, FinanceReport> {
    override fun toDomain(model: JdbcFinanceReportModel): FinanceReport {
        return FinanceReport(
            id = model.id,
            title = model.title,
            description = model.description,
            date = model.date
        )
    }

    override fun toModel(entity: FinanceReport): JdbcFinanceReportModel {
        return JdbcFinanceReportModel(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            date = entity.date
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("date")
    }
}