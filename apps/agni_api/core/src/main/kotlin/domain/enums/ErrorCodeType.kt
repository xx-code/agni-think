package domain.enums

enum class ErrorCodeType(val value: Int) {
    NONE(0),
    BUSINESS_LOGIC(400),
    NOT_FOUND(404),
    ALREADY_EXISTS(409),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INTERNAL_SERVER_ERROR(500)
}