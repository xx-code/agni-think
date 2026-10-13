import type { User } from "~/types/ui/user"
import { toUserToken } from "~/mappers/auth"
import { toUser } from "~/mappers/user"
import { API_ROUTES } from "~/shared/routes"
import type { LoginRequest } from "~/types/api/auth"

export const useAuth = () => {
    const config = useRuntimeConfig().public
    const isLocalAuth = Boolean(config.isLocalAuth ?? true)

    const user = useState<User | null>('auth_user', () => null)

    // Local implementation - start
    async function fetchCurrentUserLocal(): Promise<User | null> {
        try {
            user.value = await ApiLinkBuilder.route(API_ROUTES.USER.CURRENT_USER)
                .mapper(toUser)
                .execute()
        } catch (e: any) {
            const status = e?.statusCode ?? e?.status
            if (status === 401 || status === 403) {
                user.value = null
            } else {
                throw e
            }
        }
        return user.value
    }

    async function loginLocal(form: LoginRequest): Promise<void> {
        await ApiLinkBuilder.route(API_ROUTES.AUTH.LOGIN)
            .mapper(toUserToken)
            .body(form)
            .execute()
        await fetchCurrentUserLocal()
        await navigateTo('/')
    }

    async function logoutLocal() {
        try {
            await ApiLinkBuilder.route(API_ROUTES.AUTH.LOGOUT).execute()
        } finally {
            user.value = null
            await navigateTo('/login')
        }
    }

    async function isAuthenticatedLocal(verify = false): Promise<boolean> {
        if (user.value && !verify) return true
        return (await fetchCurrentUserLocal()) !== null
    }

    // oidc implementation - start
    const { $oidc } = useNuxtApp()

    async function loginOidc(lang: string = "en") {
        try {
            await $oidc.signinRedirect({
                redirect_uri: config.oidcRedirectUri,
                extraQueryParams: {
                    audience: (config.oidcAudience as string)
                }
            })
        } catch (error) {
            console.error("Erreur détaillée OIDC :", error)
        }
    }

    async function handleCallbackOidc(): Promise<void> {
        try  {
            const u = await $oidc.signinRedirectCallback()
            await ApiLinkBuilder.route(API_ROUTES.AUTH.CALLBACK)
                .body({ token: u.access_token })
                .execute()
            await fetchCurrentUserLocal()
            await navigateTo('/')
        } catch (error) {
            console.error("Erreur détaillée OIDC :", error)
        }
        
    }

    async function logoutOidc(lang: string = "en") {
        try {
            await ApiLinkBuilder.route(API_ROUTES.AUTH.LOGOUT).execute()
            const origin = window.location.origin
            const redirectUri = `${origin}/login`
            user.value = null
            await $oidc.signoutRedirect({
                post_logout_redirect_uri: redirectUri
            })
        }  catch (error) {
            console.error("Erreur détaillée OIDC :", error)
        }
        
    }

    async function isAuthenticatedOidc(): Promise<boolean> {
        const oidcUser = await $oidc.getUser()
        if (!oidcUser || oidcUser.expired) {
            user.value = null
            return false
        }

        // Synchroniser l'état user si absent
        if (!user.value && oidcUser) {
            user.value = {
                userId: oidcUser.profile?.sub ?? '',
                userName: oidcUser.profile?.preferred_username ?? oidcUser.profile?.name ?? '',
                email: oidcUser.profile?.email ?? ''
            }
        }
        return !!user.value
    }

    async function getAccessTokenOidc(): Promise<string | null> {
        const u = (await $oidc.getUser()) as { access_token?: string }
        return u?.access_token ?? null
    }

    return {
        user,
        isLocalAuth,
        
        async login(formOrLang: any) {
            return isLocalAuth ? loginLocal(formOrLang) : loginOidc(formOrLang)
        },
        
        async logout(lang?: string) {
            return isLocalAuth ? logoutLocal() : logoutOidc(lang)
        },

        async isAuthenticated(verify = false) {
            return isLocalAuth ? isAuthenticatedLocal(verify) : isAuthenticatedOidc()
        },

        async fetchCurrentUser() {
            if (isLocalAuth) {
                return fetchCurrentUserLocal()
            } else {
                await isAuthenticatedOidc()
                return user.value
            }
        },

        // Méthodes spécifiques si besoin
        handleCallback: handleCallbackOidc,
        getAccessToken: getAccessTokenOidc
    }
}