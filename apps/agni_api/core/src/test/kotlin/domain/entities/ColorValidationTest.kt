package domain.entities

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ColorValidationTest {

    @Test
    fun `accepts three, six and eight digit hex values`() {
        assertEquals("#ABC", Color("#abc").formattedValue)
        assertEquals("#AABBCC", Color("#AABBCC").formattedValue)
        assertEquals("#AABBCCDD", Color("#aabbccdd").formattedValue)
    }

    @Test
    fun `trims and upper cases the input`() {
        val color = Color("  #ff8800  ")

        assertEquals("#FF8800", color.formattedValue)
        assertEquals("#FF8800", color.toString())
    }

    @Test
    fun `refuses a value without the hash prefix`() {
        val error = assertFailsWith<ValidationException.InvalidColor> { Color("FF8800") }

        assertEquals("INVALID_COLOR", error.errorKey)
        assertEquals(mapOf("color" to "FF8800"), error.metadata)
    }

    @Test
    fun `refuses non hexadecimal characters`() {
        assertFailsWith<ValidationException.InvalidColor> { Color("#GGGGGG") }
    }

    @Test
    fun `refuses a truncated hex value`() {
        assertFailsWith<ValidationException.InvalidColor> { Color("#12345") }
    }

    @Test
    fun `compares on the normalised value`() {
        assertEquals(Color("#aabbcc"), Color("#AABBCC"))
        assertEquals(Color("#aabbcc").hashCode(), Color("#AABBCC").hashCode())
        assertNotEquals(Color("#aabbcc"), Color("#aabbcd"))
    }
}