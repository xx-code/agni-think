package usecases.dto

data class DeleteOutput(
    val isInUse: Boolean ,
    val success: Boolean
) {
    companion object {
        fun inUse(): DeleteOutput {
            return DeleteOutput(true, false)
        }

        fun success(): DeleteOutput {
            return DeleteOutput(false, true)
        }
    }
}