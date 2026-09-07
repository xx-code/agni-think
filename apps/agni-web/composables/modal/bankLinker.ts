import { ModalLinkAccountWithBank } from "#components";
import type { ModalOverlayInstance } from "~/types/ui";

export function useLinkAccountWithBankModal(overlay: ModalOverlayInstance) {
    const modalLink = overlay.create(ModalLinkAccountWithBank)
    
    const open = async (callBack:() => void, accountId?: string) => {
        modalLink.open({
            accountId: accountId, 
            onClose: (refresh: boolean) => {
                if (refresh) {
                    callBack()
                }
            }
        });
    }

    const close = () => {
        modalLink.close()
    }

    return { open, close }
}