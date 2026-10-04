package persistences

import org.postgresql.util.PGobject
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.mapping.JdbcValue
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import java.sql.JDBCType

@ReadingConverter
class PgObjectToStringConverter : Converter<PGobject, String?> {
    override fun convert(source: PGobject): String? = source.value
}

@WritingConverter
class StringToJdbcValueConverter : Converter<String, JdbcValue> {
    override fun convert(source: String): JdbcValue = JdbcValue.of(source, JDBCType.OTHER)
}

@Configuration
class JdbcPersistenceConfig : AbstractJdbcConfiguration() {
    override fun userConverters(): List<Any> = listOf(
        PgObjectToStringConverter(),
        StringToJdbcValueConverter(),
    )
}