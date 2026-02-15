package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTagExtend
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTagModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.java

@Component
class QueryTagExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcTagModel, Tag>
) : BaseQueryExtendJdbcAdapter<JdbcTagModel, Tag>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM tags WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM tags WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<Tag>
    ): SqlQueryBuilder {
        val extend = query as QueryTagExtend
        val params = MapSqlParameterSource()

        if (extend.isSystem != null) {
            sqlBuilder.append(" AND is_system = :isSystem")
            params.addValue("isSystem", extend.isSystem)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcTagModel> {
        return RowMapper { rs, _ ->
            JdbcTagModel(
                id = rs.getObject("tag_id", UUID::class.java),
                name = rs.getString("value"),
                color = rs.getString("color"),
                isSystem = rs.getBoolean("is_system"),
            )
        }
    }
}