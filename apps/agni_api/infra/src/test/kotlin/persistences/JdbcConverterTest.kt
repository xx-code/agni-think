package persistences

import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.postgresql.util.PGobject
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import java.sql.JDBCType

class JdbcConverterTest {

    private val reading = PgObjectToStringConverter()
    private val writing = StringToJdbcValueConverter()

    @Test
    fun `reads a jsonb column as its raw json text`() = runBlocking {
        val pg = PGobject().apply {
            type = "jsonb"
            value = """["a","b"]"""
        }

        assertEquals("""["a","b"]""", reading.convert(pg))
    }

    @Test
    fun `reads a json column as its raw json text`() = runBlocking {
        val pg = PGobject().apply {
            type = "json"
            value = """{"period":"Day","interval":1}"""
        }

        assertEquals("""{"period":"Day","interval":1}""", reading.convert(pg))
    }

    @Test
    fun `keeps a sql null as null instead of an empty string`() = runBlocking {
        val pgNull = PGobject().apply { type = "jsonb" }

        assertNull(reading.convert(pgNull), "a NULL json column must not become an empty string")
    }

    @Test
    fun `writes a string as a jdbc value of type other`() = runBlocking {
        val converted = writing.convert("""["a"]""")

        assertEquals("""["a"]""", converted.value)
        assertEquals(JDBCType.OTHER, converted.jdbcType)
    }

    @Test
    fun `does not force jsonb on a plain text value`() = runBlocking {
        // Un `jsonb` sur une colonne `text` echouerait a l'insertion avec
        // "column is of type text but expression is of type jsonb".
        assertEquals(JDBCType.OTHER, writing.convert("un simple titre").jdbcType)
    }

    @Test
    fun `registers exactly the reading and the writing converter`() = runBlocking {
        // `userConverters()` est `protected` : Spring l'appelle, pas le code applicatif.
        val userConverters = JdbcPersistenceConfig::class.java
            .getDeclaredMethod("userConverters")
            .apply { isAccessible = true }
            .invoke(JdbcPersistenceConfig()) as List<*>

        assertEquals(
            listOf(PgObjectToStringConverter::class.java, StringToJdbcValueConverter::class.java),
            userConverters.map { it!!.javaClass }
        )
    }

    @Test
    fun `extends AbstractJdbcConfiguration so Spring registers the converters`() = runBlocking {
        // Sans cet heritage, `userConverters()` n'est jamais appele et les colonnes
        // `json`/`jsonb` arrivent en `PGobject` dans les modeles.
        assertEquals(
            AbstractJdbcConfiguration::class.java,
            JdbcPersistenceConfig::class.java.superclass
        )
    }

    @Test
    fun `declares the reading and the writing annotations`() = runBlocking {
        assertTrue(
            PgObjectToStringConverter::class.java.isAnnotationPresent(ReadingConverter::class.java),
            "PGobject -> String must be a @ReadingConverter"
        )
        assertTrue(
            StringToJdbcValueConverter::class.java.isAnnotationPresent(WritingConverter::class.java),
            "String -> JdbcValue must be a @WritingConverter"
        )
    }
}