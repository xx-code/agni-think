package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail

data class CheckingAccountDetail(val buffer: Double): IAccountDetail {

    override fun getType(): AccountType {
        return AccountType.CHECKING
    }

    override fun getDetails(): Map<String, Any> {
        return mapOf("buffer" to buffer)
    }

}
