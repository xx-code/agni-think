import { ModalEditDeductionType } from "#components";
import { deductionResponseToDeduction } from "~/mappers/deduction";
import { API_ROUTES } from "~/shared/routes";
import type { GetDeductionResponse } from "~/types/api/deduction";
import type { ModalOverlayInstance } from "~/types/ui";
import type { DeductionType } from "~/types/ui/deduction";

export function useDeductionModal(overlay: ModalOverlayInstance) {
    const modalDeductionType = overlay.create(ModalEditDeductionType)

    const open = async (callBack:() => void, id?: string) => {
        let type:DeductionType|undefined=undefined;
        if (id) {
            type = await ApiLinkBuilder.route<GetDeductionResponse>(API_ROUTES.DEDUCTIONS.GET_DEDUCTION)
                .params({id}).mapper(deductionResponseToDeduction).execute(); 
        }

        modalDeductionType.open({
            deductionType: type, 
            onClose: (refresh: boolean) => {
                if (refresh) {
                    callBack()
                }
            }
        });
    }

    const close = () => {
        modalDeductionType.close()
    }

    return { open, close }
}