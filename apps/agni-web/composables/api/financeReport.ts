import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetFinanceReportResponse } from "~/types/api/finance-report";
import { listFinanceReportsResponseToListFinanceReports } from "~/mappers/financeReport";
import type { FinanceReportType } from "~/types/ui/finance-report";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchFinanceReports(query: QueryFilterRequest): Promise<ListResponse<FinanceReportType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetFinanceReportResponse>>(API_ROUTES.FINANCE_REPORTS.GET_FINANCE_REPORTS)
        .query(query)
        .mapper(listFinanceReportsResponseToListFinanceReports)
        .execute()
}
