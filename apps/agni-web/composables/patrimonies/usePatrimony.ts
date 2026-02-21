import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

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