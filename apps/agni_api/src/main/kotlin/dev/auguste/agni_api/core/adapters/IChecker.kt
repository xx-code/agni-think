package dev.auguste.agni_api.core.adapters

import java.util.UUID

interface IChecker<T> {
    fun isInUse(entity: T): Boolean
}