import type { PatrimonyEvolutionResponse } from "~/types/api/analytics";
import type { NetworthPeriod, PatrimonyEvolution } from "~/types/ui/analytics";

export function patrimonyEvolutionResponseToPatrimonyEvolution(data: PatrimonyEvolutionResponse): PatrimonyEvolution {
    const tranformMap: Map<string, NetworthPeriod[]> = new Map()

    Object.entries(data.breakdown).forEach(([key, list]) => {
        tranformMap.set(
            key, 
            list.map((i: any) => ({ ...i, date: new Date(i.date) }))
        );
    });

    return {
        networthByPeriod: data.networthByPeriod.map(i => ({...i, date: new Date(i.date)})),
        breakdown: tranformMap
    }
}