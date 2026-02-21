package dev.auguste.agni_api.core.adapters

import java.util.UUID

interface IEmbeddingService {
    fun addEmbeddingDocument(id: UUID, document: String)
    fun deleteEmbeddingDocument(id: UUID)
}