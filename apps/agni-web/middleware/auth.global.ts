export default defineNuxtRouteMiddleware(async (to) => {
    const { isAuthenticated } = useAuth()
    const isAuth = await isAuthenticated() 

    if (isAuth && (to.path == '/login' || to.path == '/auth/callback')) return navigateTo('/', { replace: true })
    if (!isAuth && (to.path !== `/login` && to.path  !== `/auth/callback`)) return navigateTo(`/login`, { replace: true })
    
})