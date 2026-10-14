import type { AuthTokenResponse } from "~/types/api/auth";
import type { AuthToken } from "~/types/ui/auth";

export function toUserToken(data: AuthTokenResponse): AuthToken { 
    return data
}