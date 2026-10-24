package domain.convertors

import domain.entities.Entity

interface ICacheCodec<T: Entity> {
    fun encode(entity: T): Map<String, Any?>
    fun decode(entity: Map<String, Any?>): T?
}