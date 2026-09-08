import { ModalEditFinancePrinciple } from "#components";
import { financePrincipleResponseToFinancePrinciple } from "~/mappers/financePrinciple";
import { API_ROUTES } from "~/shared/routes";
import type { GetFinancePrincipleResponse } from "~/types/api/financePrinciple";
import type { ModalOverlayInstance } from "~/types/ui";

export function useFinancePrincipeModal(overlay: ModalOverlayInstance) {
    const modalFinancePrincipe = overlay.create(ModalEditFinancePrinciple)

    const open = async (callBack:() => void, id?: string) => {
        let financePrinciple; 
        if (id) {
            financePrinciple = await ApiLinkBuilder.route<GetFinancePrincipleResponse>(API_ROUTES.FINANCE_PRINCIPLES.GET_FINANCE_PRINCIPLE)
                .params({ id })
                .mapper(financePrincipleResponseToFinancePrinciple)
                .execute()
        }
         

        modalFinancePrincipe.open({
            financePrinciple: financePrinciple,
            onClose: (doRefresh: boolean) => {
                if (doRefresh)
                    callBack()
            }
        })
    }

    const close = () => {
        modalFinancePrincipe.close()
    }

    return { open, close }
}