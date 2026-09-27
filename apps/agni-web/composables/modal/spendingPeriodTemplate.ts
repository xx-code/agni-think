import { ModalEditSpendingPeriodTemplate } from "#components";
import { spendingPeriodTemplateResponseToSpendingPeriodTemplate } from "~/mappers/spending-period-template";
import { API_ROUTES } from "~/shared/routes";
import type { GetSpendingPeriodTemplateResponse } from "~/types/api/spending-period-template";
import type { ModalOverlayInstance } from "~/types/ui";
import type { SpendingPeriodTemplate } from "~/types/ui/spending-period-template";

export function useSpendingPeriodTemplateModal(overlay: ModalOverlayInstance) {
    const modalTemplate = overlay.create(ModalEditSpendingPeriodTemplate)

    const open = async (callBack:() => void, id?: string) => {
        let template: SpendingPeriodTemplate | undefined = undefined
        if (id) {
            template = await ApiLinkBuilder
                .route<GetSpendingPeriodTemplateResponse>(API_ROUTES.SPENDING_PERIOD_TEMPLATES.GET_SPENDING_PERIOD_TEMPLATE)
                .params({ id })
                .mapper(spendingPeriodTemplateResponseToSpendingPeriodTemplate)
                .execute()
        }

        modalTemplate.open({
            spendingPeriodTemplate: template,
            onClose: (refresh: boolean) => {
                if (refresh)
                    callBack()
            }
        })
    }

    const close = () => {
        modalTemplate.close()
    }

    return { open, close }
}
