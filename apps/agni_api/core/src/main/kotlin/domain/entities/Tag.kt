package domain.entities

import java.util.UUID

class Tag(
    id: UUID = UUID.randomUUID(),
    value: String,
    color: Color,
    isSystem: Boolean = false,
    isArchived: Boolean = false
) : Entity(id = id) {

    var value by cleanObservable(value, this)

    var color by cleanObservable(color, this)

    var isSystem by cleanObservable(isSystem, this)

    var isArchived by cleanObservable(isArchived, this)
}