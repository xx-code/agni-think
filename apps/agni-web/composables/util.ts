import type { AsyncDataExecuteOptions } from "#app/composables/asyncData"

export const useApiLink = () => {
    const config = useRuntimeConfig()
    console.log('Calling API at', process.server ? config.api : config.public.apiBase)
    return process.server ? config.api : config.public.apiBase 
} 
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
    refresh: (opts?: AsyncDataExecuteOptions) => Promise<void> 
}

export function formatCurrency(value: number) {
    return new Intl.NumberFormat('en-US', {style: 'currency',currency: 'CAD'}).format(value)
}