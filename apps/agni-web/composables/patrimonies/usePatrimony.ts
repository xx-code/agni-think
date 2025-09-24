import type { GetPatrimonyRequest, GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyDetailType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function usePatrimony(patrimonyId: string): UseApiFetchReturn<PatrimonyDetailType> {
    const query: GetPatrimonyRequest = {
        period: 'Month',
        periodTime: 1
    }
    const { data, error, refresh } = useFetch(`/api/patrimonies/${patrimonyId}`, {
        method: 'GET',
        query: query,
        transform: (data: GetPatrimonyResponse) => {
            return {
                id: data.id,
                title: data.title,
                amount: data.amount,
                currentBalance: data.currentSnapshotBalance,
                lastSnapshotBalance: data.pastSnapshotBalance,
                type: data.type as TypePatrimony,
                accounts: data.accounts.map(i => ({
                    accountId: i.accountId,
                    balance: i.balance,
                    title: i.title,
                    pastBalance: i.pastBalance
                }))
            } satisfies PatrimonyDetailType 
        }
    })

    return { data, error, refresh }
}


export async function fetchPatrimony(patrimonyId: string): Promise<PatrimonyDetailType> {
    const query: GetPatrimonyRequest = {
        period: 'Month',
        periodTime: 1
    }

    const res = await $fetch<GetPatrimonyResponse>(`/api/patrimonies/${patrimonyId}`, {
        query: query,
        method: 'GET'
    })

    return {
        id: res.id,
        title: res.title,
        amount: res.amount,
        currentBalance: res.currentSnapshotBalance,
        lastSnapshotBalance: res.pastSnapshotBalance,
        type: res.type as TypePatrimony,
        accounts: res.accounts.map(i => ({
            accountId: i.accountId,
            balance: i.balance,
            pastBalance: i.pastBalance,
            title: i.title
        }))
    }
}