import type { GetEstimationLeftAmountResponse, GetProvisionSummaryResponse, GetSavingAnalysticResponse, GetScheduleInvoiceSummaryResponse, PatrimonyEvolutionResponse } from "~/types/api/analytics";
import type { EstimationLeftAmountType, NetworthPeriod, PatrimonyEvolution, ProvisionSummary, SavingAnalysticType, ScheduleInvoiceSummary } from "~/types/ui/analytics";

export function estimationLeftAmountResponseToEstimationLeftAmount(data: GetEstimationLeftAmountResponse): EstimationLeftAmountType {
    return { estimateAmount: data.estimateAmount };
}

export function savingAnalyticResponseToSavingAnalytic(data: GetSavingAnalysticResponse): SavingAnalysticType {
    return {
        savings: data.savings,
        investments: data.investments,
        savingRates: data.savingRates,
        investementRates: data.investmentRates
    };
}

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

export function provisionSummaryResponseToProvisionSummary(data: GetProvisionSummaryResponse): ProvisionSummary {
    return data
}

export function scheduleInvoiceSummaryResponseToScheduleInvoiceSummary(data: GetScheduleInvoiceSummaryResponse): ScheduleInvoiceSummary {
    return data
}