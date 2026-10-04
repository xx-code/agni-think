package domain.enums

import domain.exceptions.ValidationException

enum class IntensityEmotionalDesirType {
    INDIFFERENT,
    PLEASURE,
    DESIR,
    OBSESSION,
    FOMO;

    companion object {
        fun fromInt(value: Int): IntensityEmotionalDesirType {
            if (value !in 0..4)
                throw ValidationException.BadType("IntensityEmotionalDesirType", value.toString())

            return entries[value]
        }
    }
}