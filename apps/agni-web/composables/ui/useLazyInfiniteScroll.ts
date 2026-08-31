import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { ApiRouteDefinition } from "~/types/shared/routes";
import type { List } from "~/types/ui";

export default function useLazyInifinteScroll<TQuery, TResponse, TMapped>(
    routeList: ApiRouteDefinition, 
    route: ApiRouteDefinition, 
    mapperList: (data: ListResponse<TResponse>) => List<TMapped>, 
    mapper: (data: TResponse) => TMapped, 
    initQuery: QueryFilterRequest & TQuery) {

    const toast = useToast()

    const query = reactive({...initQuery})
    const data = ref<TMapped[]>([])
    const totalData = ref(0)
    const loading = ref(false)

    const hasMore = computed(() => {
        return data.value.length < totalData.value 
    })


    async function loadData() {
        if (loading.value || (data.value.length > 0 && !hasMore.value)) return

        loading.value = true
        try {
            const res = await ApiLinkBuilder
                .route<ListResponse<TResponse>>(routeList)
                .query(query)
                .mapper(mapperList)
                .execute()

            data.value.push(...res.items as any)
            totalData.value = res.total
        } catch(err: any) {
            toast.add({
                title: 'Erreur',
                description: err.message,
                color: 'error'
            })
        } finally {
            loading.value = false
        }
    } 

    function removeData(index: number) {
        if (index < 0) 
            return 

        let newData = Object.assign([] as TMapped[], data.value)
        newData.splice(index, 1)
        data.value = newData
        totalData.value -= 1
    }

    async function updateData(index:number, id: string) {
        if (index < 0)
            return 

        try {
            const res = await ApiLinkBuilder
                .route<TResponse>(route)
                .mapper(mapper)
                .params({ id })
                .execute()
            
            let newData = Object.assign([] as TMapped[], data.value)
            newData.splice(index, 1, res)
            data.value = newData
        } catch (err: any) {
            toast.add({
                title: 'Erreur',
                description: err.message,
                color: 'error'
            })
        }
    }

    function reset() {
        totalData.value = 0
        data.value = []
        Object.assign(query, initQuery)
    }

    watch(query, () => {
        loadData()
    }, { immediate: true})

    return { data, query, totalData, hasMore, loading, loadData, reset, removeData, updateData }
}