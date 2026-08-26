package dev.auguste.agni_api.core.entities

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.properties.Delegates

abstract class Entity {
    val id: UUID
    val createdAt: LocalDateTime
    var updatedAt: LocalDateTime private set
    var change: Boolean private set

    constructor(id: UUID, createdAt: LocalDateTime = LocalDateTime.now(), updatedAt: LocalDateTime = LocalDateTime.now(), change: Boolean = false) {
        this.id = id
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.change = change
    }

    fun hasChanged(): Boolean {
        return this.change
    }

    fun resetChangeState() {
        this.change = false
    }

    fun markHasChanged() {
        this.change = true
        this.updatedAt = LocalDateTime.now()
    }
}

fun<T> cleanObservable(init: T, entity: Entity,  isCorrectValue: ((T) -> Boolean)? = null, isCorrectValueError: Exception? = null) = Delegates.observable<T>(init) {
    _, props, newValue ->

    if (isCorrectValue != null) {
        if (isCorrectValueError == null) throw Error("You have to init Error when correct value")
        if (!isCorrectValue(newValue)) throw isCorrectValueError
    }

    if(props != newValue) entity.markHasChanged()
}