package dev.auguste.agni_api.core.value_objects

import java.util.UUID

data class AccountLinked(
    val accountId: UUID?,
    val bankAccountId: String,
    val bankName: String): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "accountId" to accountId.toString(),
            "bankAccountId" to bankAccountId,
            "bankName" to bankName
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): AccountLinked {
            if (map == null)
                return AccountLinked(UUID.randomUUID(), "", "")

            if (!map.containsKey("bankAccountId") && !map.containsKey("bankName"))
                return AccountLinked(UUID.randomUUID(), "", "")

            //TODO: Clean this verification
            if (map.containsKey("accountId")) {
                val accountId = map["accountId"] as? String
                if (accountId != null && accountId != "null")
                    return AccountLinked(
                        UUID.fromString(accountId),
                        map["bankAccountId"] as String,
                        map["bankName"] as String
                    )
            }


            return AccountLinked(
                null,
                map["bankAccountId"] as String,
                map["bankName"] as String
            )
        }
    }
}