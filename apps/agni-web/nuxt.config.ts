// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  typescript: {
    typeCheck: false
  },
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  modules: ['@nuxt/icon', '@nuxt/fonts', '@nuxt/ui'],
  css: [
    '@/assets/css/main.css'
  ], 
  ssr: false,
  runtimeConfig: { 
    public : {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080',
      apiAgent: process.env.NUXT_PUBLIC_API_AGENT || 'http://localhost:8000'
    } 
  }
})