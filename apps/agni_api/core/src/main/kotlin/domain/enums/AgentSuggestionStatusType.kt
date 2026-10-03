package domain.enums

import domain.exceptions.ValidationException

enum class AgentSuggestionStatusType(val value: String) {
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    PENDING("Pending");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): AgentSuggestionStatusType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("AgentSuggestionStatusType", value)
        }
    }
}