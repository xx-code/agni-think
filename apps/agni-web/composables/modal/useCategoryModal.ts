import { ModalEditCategory } from "#components"
import { categoryResponseToCategory } from "~/mappers/category"
import { API_ROUTES } from "~/shared/routes"
import type { GetCategoryResponse } from "~/types/api/category"
import type { ModalOverlayInstance } from "~/types/ui"

export default function useCategoryModal(overlay: ModalOverlayInstance) {
    const modalCategory = overlay.create(ModalEditCategory)

    const open = async (callBack:() => void, id?: string) => {
        let category; 
        if (id) {
            category = await ApiLinkBuilder.route<GetCategoryResponse>(API_ROUTES.CATEGORIES.GET_CATEGORY)
                .params({ id })
                .mapper(categoryResponseToCategory)
                .execute()
        }
         

        modalCategory.open({
            category: category,
            onClose: (doRefresh: boolean) => {
                if (doRefresh)
                    callBack()
            }
        })
    }

    const close = () => {
        modalCategory.close()
    }

    return { open, close }
}