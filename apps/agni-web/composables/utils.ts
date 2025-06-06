export const API_LINK = "http://localhost:5001/v1"
export type ErrorApi = {
    data: {
        error: {
            field: string,
            message: string
        }
    } 
}
