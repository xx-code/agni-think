export type RouteInfoType = {
    url: string,
    routeName: string
    title: string,
    subtitle: string
}

export const routeInfos: Map<string, RouteInfoType> = new Map([
    ['/', {url: '/', routeName: 'Dashboard', title: 'Bienvenu, Auguste!', subtitle: 'Il est temps de jeter un coup d\'oeil au portefeuil'}],
    ['/transactions', {url: '/transactions', title: 'Transactions', routeName: 'Transactions', subtitle: 'Un angle de vue global sur votre flux d\'argent'}],
    ['/scheduletransactions', {url: '/scheduletransactions', title: 'Schedule Transaction', routeName: 'ScheduleTransactions', subtitle: 'Voyons les depenses a venir'}],
    ['/wallets', {url: '/wallets', title: 'Vos comptes', routeName: 'Wallets', subtitle: 'Rien ne fait plus mal qu’au portefeuille. Alors protège-le'}],
    ['/budgets', {url: '/budgets', title: 'Budgets', routeName: 'Budgets', subtitle: 'Gere ton budget comme un pro'}],
    ['/goals', {url: '/goals', title: 'But d\'epargne', routeName: 'Goals', subtitle: 'Prêt à mettre un peu d\'argent de côté pour le plaisir.'}],
    ['/analystics', {url: '/analystics', title: 'Analytics', routeName: 'Analytics', subtitle: ''}],
    ['/patrimonies', {url: '/patrimonies', title: 'Patrimoine', 
        routeName: 'Patrimoine', subtitle: 'Toujours garder un oeil sur la construction de ton patrimoine. Ta vrai richesse!'}],
    ['/settings', {url: '/settings', title: 'Parametres', routeName: 'Settings', subtitle: 'Gestion des constantes de l\'application'}],
])

export const useCurrentRouteInfo = (): Ref<RouteInfoType|undefined> => {
    const routeInfo:Ref<RouteInfoType|undefined> = ref(undefined) 
    const route = useRoute()

    const update = () => {
        routeInfo.value = routeInfos.get(route.path);
    }

    update()

    watch(() => route.path, update)
       
    return routeInfo
}