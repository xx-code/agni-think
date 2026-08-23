import type { GetAccountResponse, GetContributionTypeResponse, GetInternalTypeResponse, GetManagementTypeResponse } from "~/types/api/internal"
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder"
import { API_ROUTES } from "~/shared/routes"

export async function fetchFinancePolicyRiskTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.FINANCE_POLICY_RISK_TYPE)
        .execute()
}

export async function fetchIncomeSourceFrequencyTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.INCOME_SOURCE_FREQUENCY_TYPE)
        .execute()
}

export async function fetchIncomeSourceTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.INCOME_SOURCE_TYPE)
        .execute()
}

export async function fetchPeriodRuleLevelTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PRIORITY_RULE_LEVEL_TYPE)
        .execute()
}

export async function fetchPrincipleType(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PRINCIPLE_TYPE)
        .execute()
}

export async function fetchAccounts(): Promise<GetAccountResponse[]> {
    return await ApiLinkBuilder
        .route<GetAccountResponse[]>(API_ROUTES.INTERNALS.ACCOUNT_TYPE)
        .execute()
}

export async function fetchContributionTypes(): Promise<GetContributionTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetContributionTypeResponse[]>(API_ROUTES.INTERNALS.CONTRIBUTION_TYPE)
        .execute()
}

export async function fetchImportanceTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.IMPORTANCE_TYPE)
        .execute()
}

export async function fetcheIntensityDesirTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.INTENSITY_DESIR_TYPE)
        .execute()
}

export async function fetchManagementAccounts(): Promise<GetManagementTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetManagementTypeResponse[]>(API_ROUTES.INTERNALS.MANAGEMENT_ACCOUNT_TYPE)
        .execute()
}

export async function fetchPeriodTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PERIOD_TYPE)
        .execute()
}

export async function fetchTransactionTypes(): Promise<GetInternalTypeResponse[]> {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.TRANSACTION_TYPE)
        .execute()
}
