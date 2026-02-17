import type { GetInternalTypeResponse } from "~/types/api/internal"

export async function fetchFinancePolicyRiskTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch('/api/internals/finance-policy-risk-types', {
        method: 'GET'
    })
}

export async function fetchIncomeSourceFrequencyTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch('/api/internals/income-source-frequency-types', {
        method: 'GET'
    })
}

export async function fetchIncomeSourceTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch('/api/internals/income-source-types', {
        method: 'GET'
    })
}

export async function fetchPeriodRuleLevelTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch('/api/internals/period-rule-level-types', {
        method: 'GET'
    })
}

export async function fetchPrincipleType(): Promise<GetInternalTypeResponse[]> {
    return await $fetch('/api/internals/principle-types', {
        method: 'GET'
    })
}