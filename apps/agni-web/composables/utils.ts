import type { AsyncDataExecuteOptions } from "#app/composables/asyncData"

export const API_LINK = () => {
    const config = useRuntimeConfig()
    return config.public.api
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