import type { ApiRouteDefinition } from "~/types/shared/routes"

export class ApiLinkBuilder<TResponse = any, TMapped = TResponse>{
    private url: string
    private method: ApiRouteDefinition['method']
    private bodyData?: any
    private queryData?: any
    private mapperFn?: (data: TResponse) => TMapped

    constructor(route: ApiRouteDefinition) {
        this.url = route.serverPath
        this.method = route.method
    }

    public static route<T = any>(route: ApiRouteDefinition): ApiLinkBuilder<T, T> {
        return new ApiLinkBuilder<T, T>(route)
    }

    public params(params: Record<string, string | number>): this {
        Object.entries(params).forEach(([key, value]) => {
            this.url = this.url.replace(`:${key}`, String(value)) 
        })
        return this
    }

    public body(data: any): this {
        this.bodyData = data
        return this
    }

    public query(data: any): this {
        this.queryData = data
        return this
    }

    public mapper<R>(fn: (data: TResponse) => R): ApiLinkBuilder<TResponse, R> {
        this.mapperFn = fn as any
        return this as any
    }

    public buildOptions() {
        return {
            url: this.url,
            method: this.method,
            body: this.bodyData,
            query: this.queryData
        }
    }

    public async execute(): Promise<TMapped> {
        const options = this.buildOptions()
        const res = await $fetch<TResponse>(options.url, {
            method: options.method,
            body: options.body,
            query: options.query
        })

        return this.mapperFn ? this.mapperFn(res) : (res as unknown as TMapped)
    }
}  