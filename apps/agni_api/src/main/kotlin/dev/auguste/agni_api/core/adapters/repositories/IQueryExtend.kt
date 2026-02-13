package dev.auguste.agni_api.core.adapters.repositories

import dev.auguste.agni_api.core.entities.Entity

interface IQueryExtend<T: Entity> {
    fun isStatisfy(entity: T): Boolean
}