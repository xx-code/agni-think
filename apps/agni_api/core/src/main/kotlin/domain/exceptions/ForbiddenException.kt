package domain.exceptions

import domain.enums.ErrorCodeType

sealed class ForbiddenException(errorKey: String, metadata: Map<String, Any>)
    : BaseException(errorKey, code = ErrorCodeType.FORBIDDEN, metadata = metadata) {
    class UseCase: ForbiddenException("OPERATION_FORBIDDEN", mapOf())
}