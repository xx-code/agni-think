import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetFinanceReportResponse } from "~/types/api/finance-report";
import type { FinanceReportType } from "~/types/ui/finance-report";

export async function fetchFinanceReports(query: QueryFilterRequest): Promise<ListResponse<FinanceReportType>> {
    const res = await $fetch<ListResponse<GetFinanceReportResponse>>(`${getApiBase()}/finance-reports`, {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            title: i.title,
            description: i.description,
            date: new Date(i.date)
        } satisfies FinanceReportType)),
        total: res.total
    }
}