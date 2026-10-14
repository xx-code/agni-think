import { UserManager, WebStorageStateStore } from "oidc-client-ts"

export default defineNuxtPlugin(() => {
    const cfg = useRuntimeConfig().public

    const mgr = new UserManager({
        authority: cfg.authority,
        client_id: cfg.clientId,
        redirect_uri: cfg.redirectUri,
        post_logout_redirect_uri: cfg.postLogoutRedirectUri,
        response_type: 'code',
        scope: cfg.oidcScope,
        loadUserInfo: true,
        userStore: new WebStorageStateStore({ store: window.sessionStorage }),
        automaticSilentRenew: true
    }) 
    
    return {
        provide: { oidc: mgr }
    }
})