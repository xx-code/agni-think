// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  modules: ['@nuxt/icon', '@nuxt/image', '@nuxt/fonts', '@nuxt/ui'],
  css: [
    '@/assets/css/main.css'
  ],
  ui: {
  }
})