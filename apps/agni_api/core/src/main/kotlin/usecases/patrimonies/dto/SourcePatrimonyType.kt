package usecases.patrimonies.dto

import domain.exceptions.ValidationException

enum class SourcePatrimonyType(val value: String) {
    PATRIMONY("Patrimony"),
    FUND("Fund"),
    PROVISION("Provision");

    companion object {
        fun fromString(value: String): SourcePatrimonyType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("SourcePatrimonyType", value)
        }
    }
}