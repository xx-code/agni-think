import { ModalEditSpendingPeriod } from "#components";
import { spendingPeriodResponseToSpendingPeriod } from "~/mappers/spending-period";
import { API_ROUTES } from "~/shared/routes";
import type { GetSpendingPeriodResponse } from "~/types/api/spending-period";
import type { ModalOverlayInstance } from "~/types/ui";
import type { SpendingPeriod } from "~/types/ui/spending-period";

export function useSpendingPeriodModal(overlay: ModalOverlayInstance) {
    const modalPeriod = overlay.create(ModalEditSpendingPeriod)

    const open = async (callBack:() => void, id: string) => {
        const period: SpendingPeriod = await ApiLinkBuilder
            .route<GetSpendingPeriodResponse>(API_ROUTES.SPENDING_PERIOD.GET_SPENDING_PERIOD)
            .params({ id })
            .mapper(spendingPeriodResponseToSpendingPeriod)
            .execute()

        modalPeriod.open({
            period,
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
