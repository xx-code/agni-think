package domain.entities

import java.time.LocalDateTime
import java.util.UUID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Base for entity of the domain
 *
 * who save and handle all basic entity behavior times of the creation and entity trackable value change
 **/
abstract class Entity {
    val id: UUID
    var createdAt: LocalDateTime private set
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

    fun initDate(createDate: LocalDateTime, updateDate: LocalDateTime) {
        createdAt = createDate
        updatedAt = updateDate
    }
}

/**
 * Exposes [init] as an observable property guarded by [isCorrectValue].
 *
 * [valueError] is a factory invoked with the refused value, so the thrown error can describe what
 * was actually rejected instead of the value held at construction time.
 *
 * The value is only written once [isCorrectValue] accepted it, unlike
 * `Delegates.observable` which assigns before calling back: a refused assignment therefore leaves
 * the entity untouched and does not flag it as changed.
 */
fun <T> cleanObservable(
    init: T,
    entity: Entity,
    isCorrectValue: ((T) -> Boolean)? = null,
    valueError: ((T) -> Exception)? = null
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {

    private var value = init

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        if (isCorrectValue != null) {
            if (valueError == null) throw Error("You have to init Error when correct value")
            if (!isCorrectValue(newValue)) throw valueError(newValue)
        }

        val oldValue = value
        value = newValue

        if (oldValue != newValue) entity.markHasChanged()
    }
}