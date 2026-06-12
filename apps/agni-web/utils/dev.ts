export enum TypeLog {
    WARN,
    DEBUG,
    INFO,
}

const LOG_STYLES: Record<TypeLog, string> = {
    [TypeLog.WARN]: 'color: black; background-color: yellow; font-weight: bold; padding: 4px;',
    [TypeLog.DEBUG]: 'color: white; background-color: blue; font-weight: bold; padding: 4px;',
    [TypeLog.INFO]: 'color: white; background-color: green; font-weight: bold; padding: 4px;',
}

export function printLog(
    description: string,
    error?: unknown,
    type: TypeLog = TypeLog.WARN
) {
    const env = useRuntimeConfig().public.appEnv

    if (env === "Production" && type !== TypeLog.INFO) return

    const errorMessage =
        error instanceof Error
            ? error.message
            : error
            ? JSON.stringify(error)
            : ''

    const message = `Message: ${description}${errorMessage ? ` | Error: ${errorMessage}` : ''}`

    console.log(`%c${TypeLog[type]}: ${message}`, LOG_STYLES[type])
}