// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  typescript: {
    typeCheck: false
  },
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  modules: [
    '@nuxt/icon', 
    '@nuxt/fonts', 
    '@nuxt/ui', 
    (process.env.NODE_ENV !== 'production' ? '@nuxt/test-utils/module' : '')
  ],
  css: [
    '@/assets/css/main.css'
  ], 
  ssr: false,
  runtimeConfig: { 
    public : {
      env: process.env.NODE_ENV,
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/v2',
      apiAgent: process.env.NUXT_PUBLIC_API_AGENT || 'http://localhost:8000',
      oidcAuthority: process.env.NUXT_PUBLIC_AUTHORITY || '',
      oidcClientId: process.env.NUXT_PUBLIC_CLIENT_ID || '',
      oidcAudience: process.env.NUXT_PUBLIC_AUDIENCE || '',
      oidcRedirectUri: process.env.NUXT_PUBLIC_REDIRECT_URI || '',
      oidcPostLogoutRedirectUri: process.env.NUXT_POST_LOGOUT_REDIRECT_URI || '',
      oidcScope: 'openid profile email offline_access',
      isLocalAuth: process.env.NUXT_PUBLIC_IS_LOCAL_AUTH || false
    } 
  }
})