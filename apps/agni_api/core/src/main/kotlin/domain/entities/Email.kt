package domain.entities

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import domain.exceptions.ValidationException

data class Email @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    private val email: String
) {
    @get:JsonValue
    val normalizedValue: String = email.trim()

    init {
        if (!EMAIL_REGEX.matches(normalizedValue))
            throw ValidationException.InvalidEmail(email)
    }

    override fun toString(): String = normalizedValue

    override fun equals(other: Any?): Boolean {
        return other is Email && other.normalizedValue == normalizedValue
    }

    override fun hashCode(): Int {
        return normalizedValue.hashCode()
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }
}
