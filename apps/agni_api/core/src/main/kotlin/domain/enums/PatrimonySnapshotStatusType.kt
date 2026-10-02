package domain.enums

import domain.exceptions.ValidationException

enum class PatrimonySnapshotStatusType(val value: String) {
    PENDING("Pending"),
    COMPLETED("Complete");

    companion object {
        fun fromString(value: String): PatrimonySnapshotStatusType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PatrimonySnapshotStatusType", value)
        }
    }
}