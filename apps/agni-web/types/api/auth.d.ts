export type AuthTokenResponse = {
    token: string
    type: string
}

export type LoginRequest = {
    externalId: string
    email: string
    username: string
}