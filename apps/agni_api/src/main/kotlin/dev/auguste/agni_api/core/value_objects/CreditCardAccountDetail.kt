package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail

data class CreditCardAccountDetail(val creditLimit: Double): IAccountDetail{

    override fun getType(): AccountType {
        return AccountType.CREDIT_CARD
    }

    override fun getDetails(): Map<String, Any> {
        return mapOf("creditLimit" to creditLimit)
    }
}
