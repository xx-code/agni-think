package domain.entities

import java.util.UUID

class User(
    id: UUID = UUID.randomUUID(),
    val externalUserId: String,
    username: String,
    email: Email
): Entity(id) {
    var username by cleanObservable(username, this)
    var email by cleanObservable(email, this)
}