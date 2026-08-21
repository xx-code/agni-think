import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { TypePatrimony } from "~/types/constants/patrimony";
import type { PatrimonyCard, PatrimonyType } from "~/types/ui/patrimony";

const computeEvolution = (pastBalance: number, currentBalance: number) => {
        if (pastBalance === 0) 
            return currentBalance > 0 ? 100 : 0 

        return ((currentBalance - pastBalance) / Math.abs(pastBalance)) * 100
    }

export function patrimonyResponseToPatrimony(data: GetPatrimonyResponse): PatrimonyType {
    return {
        ...data, 
        type: data.type as TypePatrimony,
        evolution: computeEvolution(data.pastBalance, data.currentBalance)
    }
}

export function patrimonyToPatrimonyCard(data: PatrimonyType): PatrimonyCard {
    return {
        id: data.id,
        title: data.title,
        description: "",
        balance: data.currentBalance,
        evolution: computeEvolution(data.pastBalance, data.currentBalance),
        isFund: data.totalFund,
        type: data.type,
    }
}