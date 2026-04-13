package dev.auguste.agni_api.core.adapters

import java.util.UUID

data class EmbeddingDocument(
    val id: UUID,
    val document: String
)

interface IEmbeddingService {
    fun addEmbeddingDocument(collectionName: String, embeddings: List<EmbeddingDocument>)
    fun deleteEmbeddingDocument(collectionName: String, id: UUID)
}