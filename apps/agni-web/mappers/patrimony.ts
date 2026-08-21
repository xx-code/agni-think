import type { PatrimonyCard, PatrimonyType } from "~/types/ui/patrimony";

export function patrimonyToPatrimonyCard(data: PatrimonyType): PatrimonyCard {
    const base = data.type === 'Asset' ? data.lastSnapshotBalance : data.currentBalance
    const current = data.type === 'Liability' ? data.currentBalance : data.lastSnapshotBalance
    const computeEvolution = () => {
        if (data.lastSnapshotBalance === 0) 
            return data.currentBalance > 0 ? 100 : 0 

        return ((data.currentBalance - data.lastSnapshotBalance) / Math.abs(data.lastSnapshotBalance)) * 100
    }
    return {
        id: data.id,
        title: data.title,
        description: "",
        balance: data.currentBalance,
        evolution: computeEvolution(),
        type: data.type,
    }
}