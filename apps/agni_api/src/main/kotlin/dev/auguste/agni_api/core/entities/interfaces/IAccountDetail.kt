package dev.auguste.agni_api.core.entities.interfaces

import dev.auguste.agni_api.core.entities.enums.AccountType

interface IAccountDetail {
    fun getType(): AccountType
    fun getDetails(): Map<String, Any>
}