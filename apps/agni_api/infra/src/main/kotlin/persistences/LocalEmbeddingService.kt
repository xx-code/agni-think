package persistences

import adapters.repositories.IEmbeddingService
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel
import org.springframework.stereotype.Service

@Service
class LocalEmbeddingService : IEmbeddingService {

    private val model = BgeSmallEnV15EmbeddingModel()

    override val dimension: Int = 384

    override fun embed(text: String): List<Float> =
        model.embed(text).content().vector().toList()

    override fun embedAll(texts: List<String>): List<List<Float>> =
        model.embedAll(texts.map { TextSegment.from(it) })
            .content()
            .map { it.vector().toList() }
}