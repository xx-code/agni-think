package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryCategoryExtend
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcCategoryModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class QueryCategoryExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcCategoryModel, Category>,
) : BaseQueryExtendJdbcAdapter<JdbcCategoryModel, Category>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM categories WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM categories WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<Category>
    ): SqlQueryBuilder {
        val extend = query as QueryCategoryExtend
        val params = MapSqlParameterSource()

        if (extend.isSystem != null) {
            sqlBuilder.append(" AND is_system = :isSystem")
            params.addValue("isSystem", extend.isSystem)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcCategoryModel> {
        return RowMapper { rs, _ ->
            JdbcCategoryModel(
                id = rs.getObject("category_id", UUID::class.java),
                name = rs.getString("title"),
                color = rs.getString("color"),
                icon = rs.getString("icon_id"),
                isSystem = rs.getBoolean("is_system"),
            )
        }
    }
}