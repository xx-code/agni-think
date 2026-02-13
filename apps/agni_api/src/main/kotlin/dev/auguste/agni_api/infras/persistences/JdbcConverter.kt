package dev.auguste.agni_api.infras.persistences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.mapping.JdbcValue
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import java.sql.JDBCType
import java.util.UUID

@ReadingConverter
class PGobjectToStringConverter : Converter<PGobject, String> {
    override fun convert(source: PGobject): String {
        return source.value ?: ""
    }
}

@ReadingConverter
class PGobjectToSetUUIDConverter(private val mapper: ObjectMapper) : Converter<PGobject, Set<UUID>> {
    override fun convert(source: PGobject): Set<UUID> {
        return source.value?.let {
            val ids = mapper.readValue<List<String>>(it)
            ids.map { str -> UUID.fromString(str) }.toSet()
        } ?: emptySet()
    }
}


@WritingConverter
class StringToPGobjectConvert : Converter<String, JdbcValue> {
    override fun convert(source: String): JdbcValue {
        return JdbcValue.of(source, JDBCType.OTHER)
    }
}

@WritingConverter
class SetUUIDToPGobjectConverter(
   private val objectMapper: ObjectMapper,
) : Converter<Set<UUID>, JdbcValue> {
    override fun convert(source: Set<UUID>): JdbcValue {
        val pgObject = PGobject().apply {
            type = "jsonb"
            value = objectMapper.writeValueAsString(source.map { it.toString() })
        }

        return JdbcValue.of(pgObject, JDBCType.OTHER)
    }
}

@Configuration
class JdbcPersistenceConfig : AbstractJdbcConfiguration() {
    private val mapper = jacksonObjectMapper()

    override fun userConverters(): List<Any> {
        return listOf(
            PGobjectToStringConverter(),
            StringToPGobjectConvert(),
            PGobjectToSetUUIDConverter(mapper),
            SetUUIDToPGobjectConverter(mapper)
        )

    }
}