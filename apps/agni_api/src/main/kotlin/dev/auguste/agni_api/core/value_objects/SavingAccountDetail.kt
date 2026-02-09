package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail

data class SavingAccountDetail(
    val secureAmount: Double
): IAccountDetail {
    override fun getType(): AccountType {
        return AccountType.SAVING
    }

    override fun toMap(): Map<String, Any> {
        return mapOf("secure_amount" to secureAmount)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): IAccountDetail {
            if (map == null)
                return SavingAccountDetail(secureAmount = 0.0)

            if (!map.containsKey("secure_amount"))
                return SavingAccountDetail(secureAmount = 0.0)

            var secureAmount = map["secure_amount"]
            if (secureAmount is Int)
                secureAmount = secureAmount.toDouble()

            return SavingAccountDetail(secureAmount = secureAmount as Double)
        }
    }

}