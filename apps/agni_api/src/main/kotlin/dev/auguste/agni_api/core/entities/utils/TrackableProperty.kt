package dev.auguste.agni_api.core.entities.utils

import kotlin.reflect.KProperty

sealed class TrackableProperty<T>(value: T, onChange: () -> Unit) {
    private var value: T = value
    private var onChange: () -> Unit = onChange

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value != value) {
            this.value = value
            onChange()
        }
    }
}
