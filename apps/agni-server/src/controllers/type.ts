export type ApiError = {
    field: string,
    message: string
}

export type ApiResponse = {
    success: boolean,
    statusCode: number,
    data: any | undefined,
    error: ApiError | undefined,
    errors: ApiError[] | undefined
}

export function initApiResponse(): ApiResponse {
    return {
        success: false,
        statusCode: 0,
        data: undefined,
        error: undefined,
        errors: undefined
    }
}