package domain.exceptions

import domain.enums.ErrorCodeType
import java.util.UUID

/**
 * Lookup failures. No leaf class is needed per entity: the four factories below build the
 * `errorKey` from the entity name so every entity shares the same shape.
 *
 * `errorKey` is `<ENTITY>_NOT_FOUND` (or `MANY_<ENTITY>_NOT_FOUND` for collections).
 */
sealed class NotFoundException(errorKey: String, metadata: Map<String, Any>)
    : BaseException(errorKey, code = ErrorCodeType.NOT_FOUND, metadata = metadata) {

    class SingleEntity(id: UUID, entityEntity: String) :
        NotFoundException("${entityEntity.uppercase()}_NOT_FOUND", mapOf("id" to id.toString()))

    class ManyEntities(ids: List<UUID>, entityEntity: String) : NotFoundException(
        "MANY_${entityEntity.uppercase()}_NOT_FOUND",
        mapOf("ids" to ids.joinToString(SEPARATOR)),
    )

    class EntityByOtherField(fieldName: String, value: Any, entityEntity: String) :
        NotFoundException("${entityEntity.uppercase()}_NOT_FOUND", mapOf(fieldName to value))

    class EntitiesByOtherField(fields: Map<String, Any>, entityEntity: String) :
        NotFoundException("MANY_${entityEntity.uppercase()}_NOT_FOUND", fields)

    companion object {
        const val SEPARATOR = "; "
    }
}
