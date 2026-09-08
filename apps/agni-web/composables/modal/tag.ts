import { ModalEditTag } from "#components";
import { tagResponseToTag } from "~/mappers/tag";
import { API_ROUTES } from "~/shared/routes";
import type { GetTagResponse } from "~/types/api/tag";
import type { ModalOverlayInstance } from "~/types/ui";

export function useTagModal(overlay: ModalOverlayInstance) {
    const modalTag = overlay.create(ModalEditTag)

    const open = async (callBack:() => void, id?: string) => {
        let tag; 
        if (id) {
            tag = await ApiLinkBuilder.route<GetTagResponse>(API_ROUTES.TAGS.GET_TAG)
                .params({ id })
                .mapper(tagResponseToTag)
                .execute()
        }
         

        modalTag.open({
            tag: tag,
            onClose: (doRefresh: boolean) => {
                if (doRefresh)
                    callBack()
            }
        })
    }

    const close = () => {
        modalTag.close()
    }

    return { open, close }
}