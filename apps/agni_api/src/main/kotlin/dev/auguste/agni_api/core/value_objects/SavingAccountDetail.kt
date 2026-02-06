package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail

data class SavingAccountDetail(
    val secureAmount: Double
): IAccountDetail {
    override fun getType(): AccountType {
        return AccountType.SAVING
    }

    override fun getDetails(): Map<String, Any> {
        return mapOf("secureAmount" to secureAmount)
    }

}