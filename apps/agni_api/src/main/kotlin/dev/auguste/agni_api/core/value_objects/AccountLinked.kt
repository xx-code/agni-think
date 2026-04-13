package dev.auguste.agni_api.core.value_objects

import java.util.UUID

data class AccountLinked(val accountId: UUID, val bankAccountId: String): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "accountId" to accountId.toString(),
            "bankAccountId" to bankAccountId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): AccountLinked {
            if (map == null)
                return AccountLinked(UUID.randomUUID(), "")

            if (!map.containsKey("accountId")  || !map.containsKey("bankAccountId"))
                return AccountLinked(UUID.randomUUID(), "")

            return AccountLinked(
                UUID.fromString(map["accountId"] as String),
                map["bankAccountId"] as String
            )
        }
    }
}