export function getApiBase(): string {
    const runtimeConfig = useRuntimeConfig()
    return runtimeConfig.public.apiBase
}

export function getApiAgent(): string {
    const runtimeConfig = useRuntimeConfig()
    return runtimeConfig.public.apiAgent
}

export function getEnviromnent(): 'PRODUCTION' | 'DEMO' | 'DEVELOPMENT' {
    const cf = useRuntimeConfig().public

    if (cf.env === 'development')
        return 'DEVELOPMENT'

    if (cf.isLocalAuth && cf.env === 'production') 
        return 'DEMO'

    return 'PRODUCTION'
}