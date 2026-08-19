package dev.auguste.agni_api.core.entities

data class Color(val value: String) {
    val formattedValue: String = value.trim().uppercase()

    init {
        require(HEX_COLOR_REGEX.matches(formattedValue)) {
            DomainException.Validation.InvalidColor(value)
        }
    }

    override fun toString(): String {
        return formattedValue
    }

    override fun equals(other: Any?): Boolean {
        return other is Color && other.formattedValue == formattedValue
    }

    override fun hashCode(): Int {
        return formattedValue.hashCode()
    }

    companion object {
        private val HEX_COLOR_REGEX = "^#([0-9A-F]{3}|[0-9A-F]{6}|[0-9A-F]{8})$".toRegex()
    }
}