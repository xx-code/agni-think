package adapters.dto

import java.util.UUID

data class VectorDocumentInput(
    val id: UUID,
    val document: String
)
