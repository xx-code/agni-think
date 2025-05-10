import Axios, { InternalAxiosRequestConfig } from 'axios';

export type CallApiError = {
    statusCode: number
    message: string
}

function authRequestInterceptor(config: InternalAxiosRequestConfig) {
    if (config.headers) {
        config.headers.Accept = 'application/json'
    }

    config.withCredentials = true
    return config
}

export const api = Axios.create({
    baseURL: 'http://localhost:3000/v1',
    withCredentials: true
})

api.interceptors.request.use(authRequestInterceptor)
api.interceptors.response.use(
    (response) => {
        return response.data.data
    },
    (error) => {
        const message = error.reponse?.data?.message || error.message // Server error || call api error

        const formatError: CallApiError = {
            statusCode: error.response?.status,
            message: message
        } 
        return Promise.reject(formatError)
    }
)