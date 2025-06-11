export const API_LINK = process.env.API_LINK || "http://localhost:5002/v1"
export type ErrorApi = {
    data: {
        error: {
            field: string,
            message: string
        }
    } 
}

export interface UseApiFetchReturn<T> {
    data: Ref<T>,
    error: Ref<ErrorApi|null>,
    refresh: () => Promise<void> 
}