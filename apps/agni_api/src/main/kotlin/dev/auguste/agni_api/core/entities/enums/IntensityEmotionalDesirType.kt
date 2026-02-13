package dev.auguste.agni_api.core.entities.enums

enum class IntensityEmotionalDesirType {
    INDIFFERENT,
    PLEASURE,
    DESIR,
    OBSESSION,
    FOMO;

    companion object {
        fun fromInt(value: Int): IntensityEmotionalDesirType {
            if (value < 0 || value > 4)
                throw IllegalArgumentException("Invalid intensity emotional desir type")

            return IntensityEmotionalDesirType.entries[value]
        }
    }
}