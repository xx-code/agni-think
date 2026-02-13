import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function usePatrimony(patrimonyId: string): UseApiFetchReturn<PatrimonyType> {
   
    const { data, error, refresh } = useFetch(`/api/patrimonies/${patrimonyId}`, {
        method: 'GET',
        transform: (data: GetPatrimonyResponse) => {
            return {
                id: data.id,
                title: data.title,
                amount: data.amount,
                currentBalance: data.currentBalance,
                lastSnapshotBalance: data.pastBalance,
                type: data.type as TypePatrimony,
                accountIds: data.accountIds
            } satisfies PatrimonyType 
        }
    })

    return { data, error, refresh }
}


export async function fetchPatrimony(patrimonyId: string): Promise<PatrimonyType> {
    const res = await $fetch<GetPatrimonyResponse>(`/api/patrimonies/${patrimonyId}`, {
        method: 'GET'
    })

    return {
        id: res.id,
        title: res.title,
        amount: res.amount,
        currentBalance: res.currentBalance,
        lastSnapshotBalance: res.pastBalance,
        type: res.type as TypePatrimony,
        accountIds: res.accountIds
    }
}