package dev.auguste.agni_api.core.entities

import java.time.LocalDate
import java.util.Date
import java.util.UUID

abstract class Entity {
    val id: UUID
    val createdAt: LocalDate
    var updatedAt: LocalDate private set
    var change: Boolean private set

    constructor(id: UUID, createdAt: LocalDate = LocalDate.now(), updatedAt: LocalDate = LocalDate.now(), change: Boolean = false) {
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
        this.updatedAt = LocalDate.now()
    }

}