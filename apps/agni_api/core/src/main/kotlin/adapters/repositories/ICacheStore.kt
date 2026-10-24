package adapters.repositories

import java.time.Duration

interface ICacheStore {
    fun getMap(key: String): Map<String, Any?>?
    fun putMap(key: String, value: Map<String, Any?>?, ttl: Duration)
    fun evict(vararg keys: String)
}