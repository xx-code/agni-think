package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.adapters.IEmbeddingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EmbeddingService(
    @Value("\${ai.agent.api.url:http://localhost:8000}") private val apiAgniUrl: String
) : IEmbeddingService {

    private val restTemplate = org.springframework.web.client.RestTemplate()

    override fun addEmbeddingDocument(id: UUID, document: String) {
        val payload = mapOf("id" to id.toString(), "document" to document)
        try {
            val response = restTemplate.postForEntity(
                "$apiAgniUrl/embedding-document",
                payload,
                String::class.java
            )

            if (!response.statusCode.is2xxSuccessful) {
                // Log or throw error so your EventRegister can handle the failure
                throw RuntimeException("Failed to add embedding: ${response.statusCode}")
            }
        } catch (e: Exception) {
            // Handle connection timeouts or DNS issues
            throw RuntimeException("Embedding service unreachable", e)
        }
    }

    override fun deleteEmbeddingDocument(id: UUID) {
        restTemplate.delete("$apiAgniUrl/embedding-document/$id")
    }
}