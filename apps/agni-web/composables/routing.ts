export type RouteInfoType = {
    url: string,
    routeName: string
    title: string,
    subtitle: string
}

export const routeInfos: Map<string, RouteInfoType> = new Map([
    ['/', {url: '/', routeName: 'Dashboard', title: 'Bienvenu, Auguste!', subtitle: 'Il est temps de jeter un coup d\'oeil au portefeuil'}],
    ['/transactions', {url: '/transactions', title: 'Transactions', routeName: 'Transactions', subtitle: 'Un angle de vue global sur votre flux d\'argent'}],
    ['/wallets', {url: '/wallets', title: 'Vos comptes', routeName: 'Wallets', subtitle: 'Rien ne fait plus mal qu’au portefeuille. Alors protège-le'}],
    ['/budgets', {url: '/budgets', title: '', routeName: 'Budgets', subtitle: ''}],
    ['/goals', {url: '/goals', title: '', routeName: 'Goals', subtitle: ''}],
    ['/analystics', {url: '/analystics', title: '', routeName: 'Analystics', subtitle: ''}],
    ['/settings', {url: '/settings', title: '', routeName: 'Settings', subtitle: ''}],
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