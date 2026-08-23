import type { ListResponse } from "~/types/api";
import type { GetFinanceReportResponse } from "~/types/api/finance-report";
import type { FinanceReportType } from "~/types/ui/finance-report";

export function financeReportResponseToFinanceReport(data: GetFinanceReportResponse): FinanceReportType {
    return {
        id: data.id,
        title: data.title,
        description: data.description,
        date: new Date(data.date)
    }
}

export function listFinanceReportsResponseToListFinanceReports(data: ListResponse<GetFinanceReportResponse>): ListResponse<FinanceReportType> {
    return {
        items: data.items.map(i => financeReportResponseToFinanceReport(i)),
        total: data.total
    }
}
