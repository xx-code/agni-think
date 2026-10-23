package adapters.repositories

import adapters.dto.VectorDocumentInput
import usecases.dto.CreatedOutput

interface IVectorRepository {
    fun addDocuments(collectionName: String, documents: List<VectorDocumentInput>): List<CreatedOutput>
    fun addDocument(collectionName: String, document: VectorDocumentInput): CreatedOutput
    fun removeDocument(id: String, collection: String)
    fun getDocument(id: String, collection: String): String
}