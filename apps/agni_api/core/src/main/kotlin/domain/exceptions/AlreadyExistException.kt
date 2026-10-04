package domain.exceptions

import domain.enums.ErrorCodeType

/**
 * Uniqueness failures. `errorKey` is `<ENTITY>_ALREADY_EXISTS`.
 */
sealed class AlreadyExistException(errorKey: String, metadata: Map<String, Any>)
    : BaseException(errorKey, code = ErrorCodeType.ALREADY_EXISTS, metadata = metadata) {

    class Entity(entityName: String) :
        AlreadyExistException("${entityName.uppercase()}_ALREADY_EXISTS", mapOf())

    class EntitiesByField(fields: Map<String, Any>, entityName: String) :
        AlreadyExistException("${entityName.uppercase()}_ALREADY_EXISTS", fields)
}
