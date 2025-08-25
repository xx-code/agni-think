import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyDetailType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function usePatrimony(patrimonyId: string): UseApiFetchReturn<PatrimonyDetailType> {
    const { data, error, refresh } = useFetch(`/api/patrimonies/${patrimonyId}`, {
        method: 'GET',
        transform: (data: GetPatrimonyResponse) => {
            return {
                id: data.id,
                title: data.title,
                currentBalance: data.accounts.reduce((accumulator: number, currentValue) => {
                    return accumulator + currentValue.balance
                }, 0),
                lastSnapshotBalance: data.trackBalance,
                type: data.type as TypePatrimony,
                accounts: data.accounts.map(i => ({
                    accountId: i.accountId,
                    balance: i.balance,
                    title: i.title
                }))
            } satisfies PatrimonyDetailType 
        }
    })

    return { data, error, refresh }
}

export async function fetchPatrimony(patrimonyId: string): Promise<PatrimonyDetailType> {
    const res = await $fetch<GetPatrimonyResponse>(`/api/patrimonies/${patrimonyId}`, {
        method: 'GET'
    })

    return {
        id: res.id,
        title: res.title,
        currentBalance: res.accounts.reduce((accumulator: number, currentValue) => {
            return accumulator + currentValue.balance
        }, 0),
        lastSnapshotBalance: res.trackBalance,
        type: res.type as TypePatrimony,
        accounts: res.accounts.map(i => ({
            accountId: i.accountId,
            balance: i.balance,
            title: i.title
        }))
    }
}