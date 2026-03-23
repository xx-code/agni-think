package dev.auguste.agni_api.core.entities.enums

enum class AgentSuggestionStatusType(val value: String) {
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    PENDING("Pending");

    companion object {
        fun fromString(value: String): AgentSuggestionStatusType {
            return AgentSuggestionStatusType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Agent Suggestion Status Type $value not found in enums")
        }
    }
}