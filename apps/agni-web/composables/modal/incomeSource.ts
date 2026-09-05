import { ModalEditIncomeSource } from "#components";
import { incomeSourceResponseToIncomeSource } from "~/mappers/incomeSource";
import { API_ROUTES } from "~/shared/routes";
import type { GetIncomeSourceResponse } from "~/types/api/incomeSource";
import type { ModalOverlayInstance } from "~/types/ui";
import type { IncomeSourceType } from "~/types/ui/incomeSource";

export function useIncomeSourceModel(overlay: ModalOverlayInstance) {
    const modalIncome = overlay.create(ModalEditIncomeSource)

    const open = async (callBack:() => void, id?: string) => {
        let source:IncomeSourceType|undefined=undefined;
        if (id) {
            source = await ApiLinkBuilder
            .route<GetIncomeSourceResponse>(API_ROUTES.INCOME_SOURCES.GET_INCOME_SOURCE)
            .params({id})
            .mapper(incomeSourceResponseToIncomeSource).execute(); 
        }

        modalIncome.open({
            incomeSource: source,
            onClose: (refresh: boolean) => {
                if (refresh) {
                    callBack()
                }
            }
        });
    }

    const close = () => {
        modalIncome.close()
    }

    return { open, close }
}