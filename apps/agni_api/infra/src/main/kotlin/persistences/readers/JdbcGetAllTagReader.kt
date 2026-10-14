package persistences.readers

import adapters.dto.QueryFilter
import adapters.dto.RepoList
import adapters.readers.IGetAllTagReader
import adapters.repositories.IQueryExtendBuilder
import domain.entities.Tag
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import persistences.IMapper
import persistences.JdbcQueryAdapter
import persistences.jbdc_model.JdbcTagModel
import java.util.UUID

@Component
class JdbcGetAllTagReader(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcTagModel, Tag>,
    private val queryAdapter: JdbcQueryAdapter,
): IGetAllTagReader {
    override fun read(
        query: QueryFilter,
        userId: UUID?,
        queryExtend: IQueryExtendBuilder<Tag>?
    ): RepoList<Tag> {
        val tableName = mapper.getTableName()

        var sql = StringBuilder("""
            SELECT * FROM $tableName 
            WHERE 1=1 AND (user_id IS NULL
        """.trimIndent())

        val params = MapSqlParameterSource()
        if (userId != null) {
            sql.append(" OR user_id = :userId)")
            params.addValue("userId", userId)
        } else {
            sql.append(")")
        }

        if (queryExtend != null) {
            val queryBuild = queryAdapter.getSqlStringBuilder(
                sqlBuilder = sql,
                queryBuilder = queryExtend,
                mapper = mapper,
                sharedParams = params,
            )
            sql = queryBuild.sql
        }

        val countSql = StringBuilder("SELECT COUNT(*) FROM $tableName WHERE 1=1")
        val total = jdbcTemplate.queryForObject(countSql.toString(), params, Long::class.java) ?: 0L

        val items = jdbcTemplate.query(
            sql.toString(),
            params,
            DataClassRowMapper(mapper.getModelClass())
        )

        return RepoList(
            items = items.map { mapper.toDomain(it) },
            total = total
        )
    }
}