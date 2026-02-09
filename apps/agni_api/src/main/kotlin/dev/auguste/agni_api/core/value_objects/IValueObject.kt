package dev.auguste.agni_api.core.value_objects

interface IValueObject {
    fun toMap(): Map<String, Any>
}