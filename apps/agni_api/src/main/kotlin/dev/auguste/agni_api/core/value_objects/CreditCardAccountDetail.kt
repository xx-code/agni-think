package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail

data class CreditCardAccountDetail(val creditLimit: Double): IAccountDetail{

    override fun getType(): AccountType {
        return AccountType.CREDIT_CARD
    }

    override fun toMap(): Map<String, Any> {
        return mapOf("credit_limit" to creditLimit)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): IAccountDetail {
            if (map == null)
                return CreditCardAccountDetail(creditLimit = 0.0)

            if (!map.containsKey("credit_limit"))
                return CreditCardAccountDetail(creditLimit = 0.0)

            var creditLimit = map["credit_limit"]
            if (creditLimit is Int)
                creditLimit = creditLimit.toDouble()

            return CreditCardAccountDetail(creditLimit = creditLimit as Double)
        }
    }
}
