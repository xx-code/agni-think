package adapters.repositories

import adapters.dto.VectorDocumentInput
import usecases.dto.CreatedOutput

interface IEmbeddingService {
    val dimension: Int
    fun embed(text: String): List<Float>
    fun embedAll(texts: List<String>): List<List<Float>> = texts.map(::embed)
}

interface IVectorRepository {
    fun addDocuments(collectionName: String, documents: List<VectorDocumentInput>): List<CreatedOutput>
    fun addDocument(collectionName: String, document: VectorDocumentInput): CreatedOutput
    fun removeDocument(id: String, collection: String)
    fun getDocument(id: String, collection: String): String

    fun query(question: String, collectionName: String, limit: Int = 10): String
}