package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcPatrimonySnapshotModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID
import kotlin.jvm.java


@Component
class QueryPatrimonySnapshotExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcPatrimonySnapshotModel, PatrimonySnapshot>,
): BaseQueryExtendJdbcAdapter<JdbcPatrimonySnapshotModel, PatrimonySnapshot>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM patrimony_snapshots WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM patrimony_snapshots WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<PatrimonySnapshot>
    ): SqlQueryBuilder {
        val extend = query as QueryPatrimonySnapshotExtend
        val params = MapSqlParameterSource()

        if (extend.patrimonyIds.isNotEmpty()) {
            sqlBuilder.append(" AND patrimony_id IN (:patrimonies)")
            params.addValue("patrimonies", extend.patrimonyIds)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcPatrimonySnapshotModel> {
        return RowMapper { rs, _ ->
            JdbcPatrimonySnapshotModel(
                id = rs.getObject("patrimony_snapshot_id", UUID::class.java),
                patrimonyId = rs.getObject("patrimony_id", UUID::class.java),
                balance = rs.getDouble("balance"),
                date = rs.getObject("date", LocalDate::class.java),
                status = rs.getString("status")
            )
        }
    }
}
