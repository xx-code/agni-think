export type ApiRouteDefinition = {
    serverPath: string,
    apiPath: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | '*',
    session?: 'create' | 'destroy' | 'create-oidc',
    backend?: 'main' | 'agent',
    public?: boolean
}