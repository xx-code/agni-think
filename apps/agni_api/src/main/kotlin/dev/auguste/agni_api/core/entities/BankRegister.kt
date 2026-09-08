package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.value_objects.AccountLinked
import java.util.UUID

class BankRegister(
    id: UUID = UUID.randomUUID(),
    institutionId: String,
    title: String,
    accessCode: String,
    accountsLinked: Set<AccountLinked> = setOf(),
    cursor: String = "",
    isActive: Boolean = true
): Entity(id) {
    var title by cleanObservable(title, this)
    var institutionId by cleanObservable(institutionId, this)
    var accessCode by cleanObservable(accessCode, this)
    var cursor by cleanObservable(cursor, this)
    var isActive by cleanObservable(isActive, this)
    var accountslinked by cleanObservable(accountsLinked, this)
}