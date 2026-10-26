package domain.entities

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import domain.exceptions.ValidationException

data class Color @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    private val value: String
) {
    @get:JsonValue
    val formattedValue: String = value.trim().uppercase()

    init {
        if (!HEX_COLOR_REGEX.matches(formattedValue))
            throw ValidationException.InvalidColor(value)
    }

    override fun toString(): String {
        return formattedValue
    }

    override fun equals(other: Any?): Boolean {
        return other is Color && other.formattedValue == formattedValue
    }

    override fun hashCode(): Int {
        return formattedValue.hashCode()
    }

    companion object {
        private val HEX_COLOR_REGEX = "^#([0-9A-F]{3}|[0-9A-F]{6}|[0-9A-F]{8})$".toRegex()
    }
}