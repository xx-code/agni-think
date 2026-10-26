package persistences

import adapters.dto.VectorDocumentInput
import adapters.repositories.IEmbeddingService
import adapters.repositories.IVectorRepository
import io.qdrant.client.PointIdFactory.id
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.QueryFactory.nearest
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorFactory
import io.qdrant.client.VectorsFactory.namedVectors
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.WithPayloadSelectorFactory.enable
import io.qdrant.client.WithVectorsSelectorFactory
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import io.qdrant.client.grpc.Common.PointId
import io.qdrant.client.grpc.Points.PointStruct
import io.qdrant.client.grpc.Points.QueryPoints
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import usecases.dto.CreatedOutput
import java.util.UUID

@Repository
class QdrantVectorRepository(
    @Value("\${vectordb.qdrant.host}") private val host: String,
    @Value("\${vectordb.qdrant.port:6334}") private val port: Int,
    private val embeddingService: IEmbeddingService,
) : IVectorRepository {

    private val client: QdrantClient =
        QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build())

    companion object {
        private const val DOCUMENT_KEY = "document"
        private const val VECTOR_NAME = "fast-bge-small-en"
    }

    private fun toPointId(raw: String): PointId =
        raw.toLongOrNull()?.let { id(it) } ?: id(UUID.fromString(raw))

    private fun ensureCollection(collectionName: String) {
        if (!client.collectionExistsAsync(collectionName).get()) {
            client.createCollectionAsync(
                collectionName,
                mapOf(
                    VECTOR_NAME to VectorParams.newBuilder()
                        .setDistance(Distance.Cosine)
                        .setSize(embeddingService.dimension.toLong())
                        .build()
                )
            ).get()
        }
    }

    override fun addDocuments(
        collectionName: String,
        documents: List<VectorDocumentInput>
    ): List<CreatedOutput> {
        if (documents.isEmpty()) return emptyList()
        ensureCollection(collectionName)

        val embeddings = embeddingService.embedAll(documents.map { it.document })

        val points = documents.mapIndexed { i, doc ->
            PointStruct.newBuilder()
                .setId(toPointId(doc.id.toString()))
                .setVectors(namedVectors(mapOf(VECTOR_NAME to VectorFactory.vector(embeddings[i]))))
                .putAllPayload(mapOf(DOCUMENT_KEY to value(doc.document)))
                .build()
        }

        client.upsertAsync(collectionName, points).get()
        return documents.map { CreatedOutput(it.id) }
    }

    override fun addDocument(
        collectionName: String,
        document: VectorDocumentInput
    ): CreatedOutput = addDocuments(collectionName, listOf(document)).first()

    override fun removeDocument(id: String, collection: String) {
        client.deleteAsync(collection, listOf(toPointId(id))).get()
    }

    override fun getDocument(id: String, collection: String): String {
        val points = client.retrieveAsync(
            collection,
            listOf(toPointId(id)),
            enable(true),
            WithVectorsSelectorFactory.enable(false),
            null
        ).get()

        return points.firstOrNull()
            ?.payloadMap?.get(DOCUMENT_KEY)?.stringValue
            ?: throw NoSuchElementException("Document $id introuvable dans $collection")
    }

    override fun query(question: String, collectionName: String, limit: Int): String {
        val request = QueryPoints.newBuilder()
            .setCollectionName(collectionName)
            .setQuery(nearest(embeddingService.embed(question)))
            .setUsing(VECTOR_NAME)
            .setLimit(limit.toLong())
            .setWithPayload(enable(true))
            .build()

        return client.queryAsync(request).get()
            .mapNotNull { it.payloadMap[DOCUMENT_KEY]?.stringValue }
            .joinToString("\n")
    }
}