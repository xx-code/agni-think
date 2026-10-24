package persistences

import adapters.repositories.ICacheStore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheStore(
    private val redis: StringRedisTemplate,
    private val mapper: ObjectMapper,
) : ICacheStore {
    private val log = LoggerFactory.getLogger(javaClass)
    private val mapType = object : TypeReference<Map<String, Any?>>() {}

    override fun getMap(key: String): Map<String, Any?>? {
        try {
            return redis.opsForValue().get(key)?.let { mapper.readValue(it, mapType) }
        } catch (e: Exception) {
            log.warn("Cache read failed key=$key: ${e.message}")
            return null
        }
    }

    override fun putMap(
        key: String,
        value: Map<String, Any?>?,
        ttl: Duration
    ) {
        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(value), ttl)
        } catch (e: Exception) {
            log.warn("Cache write failed key=$key: ${e.message}")
        }
    }

    override fun evict(vararg keys: String) {
        try { if (keys.isNotEmpty()) redis.delete(keys.toList()) }
        catch (e: Exception) { log.warn("Cache evict failed: ${e.message}") }
    }
}
