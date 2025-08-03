export type ErrorApi = {
    data: {
        errors: {
            field: string,
            message: string
        }
    } 
}

export type UseApiFetchReturn<T> = {
    data: Ref<T|null>,
    error: Ref<ErrorApi|null>,
    refresh: (opts?: AsyncDataExecuteOptions) => Promise<void> 
}