export function getApiBase(): string {
    const runtimeConfig = useRuntimeConfig()
    return runtimeConfig.public.apiBase
}

export function getApiAgent(): string {
    const runtimeConfig = useRuntimeConfig()
    return runtimeConfig.public.apiAgent
}