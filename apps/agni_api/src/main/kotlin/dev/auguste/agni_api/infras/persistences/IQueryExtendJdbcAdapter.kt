package dev.auguste.agni_api.infras.persistences

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Entity
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

data class SqlQueryBuilder(val sql: StringBuilder, val params: MapSqlParameterSource)

interface IQueryExtendJdbcAdapter<TModel, TEntity: Entity> {
    fun getSqlStringBuilder(queryFilter: QueryFilter, query: IQueryExtend<TEntity>): SqlQueryBuilder
    fun getRawMapper(): RowMapper<TModel>
    fun filter(queryFilter: QueryFilter, extend: IQueryExtend<TEntity>): List<TModel>
}