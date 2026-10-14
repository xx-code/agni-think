package domain.exceptions

import domain.enums.ErrorCodeType

sealed class UnAuthorizeException(errorKey: String, metadata: Map<String, Any>)
: BaseException(errorKey, code = ErrorCodeType.UNAUTHORIZED, metadata = metadata){
    class UserUnAuthorizedException: UnAuthorizeException("USER_UNAUTHORIZE", mapOf())
}