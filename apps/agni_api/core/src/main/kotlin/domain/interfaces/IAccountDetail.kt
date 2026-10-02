package domain.interfaces

import domain.enums.AccountType
import domain.value_objects.IValueObject

interface IAccountDetail : IValueObject {
    fun getType(): AccountType
}