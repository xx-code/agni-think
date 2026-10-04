package domain.exceptions

import domain.enums.ErrorCodeType

/**
 * Root of every domain failure.
 *
 * An exception carries no human readable text: it carries an [errorKey] (stable, machine readable,
 * used as the key inside the i18n json catalogs) plus a [metadata] map whose values are injected
 * into the localized template through `{{placeholder}}`.
 */
sealed class BaseException(
    val errorKey: String,
    val code: ErrorCodeType,
    val metadata: Map<String, Any>,
) : Exception(errorKey)

open class UnExpectedException(errorMessage: String) : BaseException(
    errorKey = "UNKNOWN_EXCEPTION",
    code = ErrorCodeType.INTERNAL_SERVER_ERROR,
    metadata = mapOf("error" to errorMessage),
)
