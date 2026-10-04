package domain.value_objects

interface IValueObject {
    fun toMap(): Map<String, Any>
}