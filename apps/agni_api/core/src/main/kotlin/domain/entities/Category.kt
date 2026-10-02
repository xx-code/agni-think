package domain.entities

import java.util.UUID


class Category(
    id: UUID = UUID.randomUUID(),
    title: String,
    icon: String,
    color: Color,
    isSystem: Boolean = false,
    isArchived: Boolean = false
): Entity(id = id) {
    var title: String by cleanObservable(title, this)

    var icon: String by cleanObservable(icon, this)

    var color: Color by cleanObservable(color, this)

    var isSystem: Boolean by cleanObservable(isSystem, this)

    var isArchived: Boolean by cleanObservable(isArchived, this)
}