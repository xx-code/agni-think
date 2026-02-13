package dev.auguste.agni_api.core.entities.enums

enum class PatrimonySnapshotStatusType(val value: String) {
    PENDING("Pending"),
    COMPLETED("Complete");

    companion object {
        fun fromString(value: String): PatrimonySnapshotStatusType {
            return PatrimonySnapshotStatusType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Patrimony Snapshot Type $value not found in enums")
        }
    }
}