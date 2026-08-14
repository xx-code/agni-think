import type { ErrorResponse } from "./api"

export type FormatedError = ErrorResponse

export type Result<T> = {
    success: boolean
    data?: T,
    error?: ErrorResponse
}