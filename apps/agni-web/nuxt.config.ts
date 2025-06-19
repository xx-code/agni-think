// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  typescript: {
    typeCheck: true
  },
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  modules: ['@nuxt/icon', '@nuxt/fonts', '@nuxt/ui'],
  css: [
    '@/assets/css/main.css'
  ],
  ui: {
  },
  runtimeConfig: {
    api: process.env.API_URL || "http://api:5001/v1",
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE  || "http://localhost:5001/v1"
    }
  }
})