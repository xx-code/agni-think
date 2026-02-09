package dev.auguste.agni_api.core.entities.interfaces

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.value_objects.IValueObject

interface IAccountDetail : IValueObject {
    fun getType(): AccountType
}