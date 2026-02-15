package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Entity
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcModel
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

internal fun <M, E> addPaginationSqlStringBuilder(
    sql: StringBuilder,
    params: MapSqlParameterSource,
    queryFilter: QueryFilter,
    mapper: IMapper<M, E>,
    isDistinctTransaction: Boolean? = null
): StringBuilder {


    val allowedSortFields = mapper.getSortField()

    if (queryFilter.sortBy.by in allowedSortFields) {
        val sortField = queryFilter.sortBy.by
        val direction = if (queryFilter.sortBy.ascending) "ASC" else "DESC"

        if (isDistinctTransaction != null && isDistinctTransaction) {
            sql.append(" ORDER BY t.transaction_id, $sortField $direction")
        } else {
            sql.append(" ORDER BY $sortField $direction")
        }

    }

    if (queryFilter.queryAll) return sql

    sql.append(" LIMIT :limit OFFSET :offset")
    params.addValue("limit", queryFilter.limit)
    params.addValue("offset", queryFilter.offset)

    return sql
}

abstract class BaseQueryExtendJdbcAdapter<M: JdbcModel, E: Entity>(
    protected val jdbcTemplate: NamedParameterJdbcTemplate,
    protected val mapper: IMapper<M, E>
) : IQueryExtendJdbcAdapter<M, E> {
    override fun filter(queryFilter: QueryFilter, extend: IQueryExtend<E>): ListOutput<M> {
        val builderCounter = getSqlStringBuilder(getSqlCountQuery(), queryFilter, extend)
        val total = jdbcTemplate.queryForObject(builderCounter.sql.toString(), builderCounter.params, Long::class.java) ?: 0

        val builder = getSqlStringBuilder(getSqlQuery(), queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)

        val rowMapper = getRawMapper()

        return ListOutput(
            jdbcTemplate.query(sql.toString(), builder.params, rowMapper),
            total = total
        )

    }
}