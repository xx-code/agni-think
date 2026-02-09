package dev.auguste.agni_api.core.entities.enums

enum class PatrimonySnapshotStatusType(val value: String) {
    PENDING("Pending"),
    COMPLETED("Complete");

    companion object {
        fun fromString(value: String): PatrimonySnapshotStatusType {
            return PatrimonySnapshotStatusType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}