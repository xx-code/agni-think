import type { GetAccountTypeResponse, GetContributionTypeResponse, GetInternalTypeResponse, GetManagementTypeResponse } from "~/types/api/internal"

export async function fetchFinancePolicyRiskTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch(`api/internals/finance-policy-risk-type`, {
        method: 'GET'
    })
}

export async function fetchIncomeSourceFrequencyTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch(`api/internals/income-source-frequency-type`, {
        method: 'GET'
    })
}

export async function fetchIncomeSourceTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch(`api/internals/income-source-type`, {
        method: 'GET'
    })
}

export async function fetchPeriodRuleLevelTypes(): Promise<GetInternalTypeResponse[]> {
    return await $fetch(`api/internals/period-rule-level-type`, {
        method: 'GET'
    })
}

export async function fetchPrincipleType(): Promise<GetInternalTypeResponse[]> {
    return await $fetch(`api/internals/principle-type`, {
        method: 'GET'
    })
}

export async function fetchAccountTypes(): Promise<GetAccountTypeResponse[]> {
    const res = await $fetch<GetAccountTypeResponse[]>(`api/internals/account-type`, {
        method: 'GET'
    })

    return res
}

export async function fetchContributionTypes(): Promise<GetContributionTypeResponse[]> {
    const res = await $fetch<GetContributionTypeResponse[]>(`api/internals/contribution-type`, {
        method: 'GET'
    })

    return res
}

export async function fetchImportanceTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>(`api/internals/importance-type`, {
        method: 'GET'
    })
    return res
}

export async function fetcheIntensityDesirTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>(`api/internals/intensity-desir-type`, {
        method: 'GET'
    })
    return res
}

export async function fetchManagementAccountTypes(): Promise<GetManagementTypeResponse[]> {
    const res = await $fetch<GetManagementTypeResponse[]>(`api/internals/management-account-type`, {
        method: 'GET'
    })

    return res
}

export async function fetchPeriodTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>(`api/internals/period-type`, {
        method: 'GET'
    })
    return res
}

export async function fetchTransactionTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>(`api/internals/transaction-type`, {
        method: 'GET'
    })
    return res
}