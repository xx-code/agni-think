import { UserManager, WebStorageStateStore } from "oidc-client-ts"

export default defineNuxtPlugin(() => {
    const cfg = useRuntimeConfig().public

    const mgr = new UserManager({
        authority: cfg.oidcAuthority,
        client_id: cfg.oidcClientId,
        redirect_uri: cfg.oidcRedirectUri,
        post_logout_redirect_uri: cfg.oidcPostLogoutRedirectUri,
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