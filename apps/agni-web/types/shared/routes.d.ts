export type ApiRouteDefinition = {
    serverPath: string,
    apiPath: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' 
}