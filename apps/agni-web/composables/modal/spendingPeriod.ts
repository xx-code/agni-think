import { ModalEditSpendingPeriod } from "#components";
import { budgetResponseToBudget, listBudgetsResponseToListBudgets } from "~/mappers/budget";
import { listScheduleInvoicesResponseToListScheduleInvoices } from "~/mappers/scheduleTransaction";
import { toSpendingPeriod } from "~/mappers/spending-period";
import { API_ROUTES } from "~/shared/routes";
import type { ListResponse } from "~/types/api";
import type { BudgetQueryFilterRequest, GetBudgetResponse } from "~/types/api/budget";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { GetSpendingPeriodResponse } from "~/types/api/spending-period";
import type { ModalOverlayInstance } from "~/types/ui";
import type { SpendingPeriod } from "~/types/ui/spending-period";

export function useSpendingPeriodModal(overlay: ModalOverlayInstance) {
    const modalPeriod = overlay.create(ModalEditSpendingPeriod)

    const open = async (callBack:() => void, id: string) => {
        const budgetQuery: BudgetQueryFilterRequest = {limit: 0, offset: 0, queryAll: true,  loadBalance: false } 
        const [period, budgets, scheduleInvoices] =  await Promise.all([
            ApiLinkBuilder
                .route<GetSpendingPeriodResponse>(API_ROUTES.SPENDING_PERIOD.GET_SPENDING_PERIOD)
                .params({ id })
                .mapper(toSpendingPeriod)
                .execute(),
            ApiLinkBuilder
                .route<ListResponse<GetBudgetResponse>>(API_ROUTES.BUDGETS.GET_BUDGETS)
                .query(budgetQuery)
                .mapper(listBudgetsResponseToListBudgets)
                .execute(),
            ApiLinkBuilder
                .route<ListResponse<GetScheduleInvoiceResponse>>(API_ROUTES.SCHEDULE_INVOICES.GET_SCHEDULE_INVOICES)
                .query({ limit: 0, offset: 0, queryAll: true})
                .mapper(listScheduleInvoicesResponseToListScheduleInvoices)
                .execute()
        ]) 

        modalPeriod.open({
            period,
            budgets: budgets.items,
            scheduleInvoices: scheduleInvoices.items,
            onClose: (refresh: boolean) => {
                if (refresh)
                    callBack()
            }
        })
    }

    const close = () => {
        modalPeriod.close()
    }

    return { open, close }
}
