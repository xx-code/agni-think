import type { ErrorResponse } from "./api"

export type Result<T> = {
    success: boolean
    data?: T,
    error?: ErrorResponse
}