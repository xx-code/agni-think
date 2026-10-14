package domain.entities

import kotlinx.coroutines.runBlocking

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EmailValidationTest {

    @Test
    fun `accepts a standard email`() = runBlocking {
        val email = Email("user@example.com")

        assertEquals("user@example.com", email.toString())
    }

    @Test
    fun `accepts plus addressing, dots and subdomains`() = runBlocking {
        assertEquals("first.last+tag@mail.sub.example.co", Email("first.last+tag@mail.sub.example.co").toString())
    }

    @Test
    fun `trims the surrounding whitespace`() = runBlocking {
        val email = Email("  user@example.com  ")

        assertEquals("user@example.com", email.toString())
    }

    @Test
    fun `refuses a value without the at sign`() = runBlocking {
        val error = assertFailsWith<ValidationException.InvalidEmail> { Email("plainaddress") }

        assertEquals("INVALID_EMAIL", error.errorKey)
        assertEquals(mapOf("email" to "plainaddress"), error.metadata)
    }

    @Test
    fun `refuses an email without a local part`()  {
        assertFailsWith<ValidationException.InvalidEmail> { Email("@example.com") }
    }

    @Test
    fun `refuses an email without a domain`()  {
        assertFailsWith<ValidationException.InvalidEmail> { Email("user@") }
    }

    @Test
    fun `refuses a domain without a top level domain`()  {
        assertFailsWith<ValidationException.InvalidEmail> { Email("user@example") }
    }

    @Test
    fun `refuses an empty value`() {
        val error = assertFailsWith<ValidationException.InvalidEmail> { Email("") }

        assertEquals(mapOf("email" to ""), error.metadata)
    }

    @Test
    fun `reports the offending raw value when whitespace makes it invalid`()  {
        val error = assertFailsWith<ValidationException.InvalidEmail> { Email("  not-an-email  ") }

        assertEquals(mapOf("email" to "  not-an-email  "), error.metadata)
    }

    @Test
    fun `compares on the normalised value`() = runBlocking {
        assertEquals(Email("user@example.com"), Email("  user@example.com  "))
        assertEquals(Email("user@example.com").hashCode(), Email("  user@example.com  ").hashCode())
        assertNotEquals(Email("user@example.com"), Email("other@example.com"))
    }
}
