import { ModalConfirm } from "#components";
import type { ConfirmDialog } from "~/types/ui/confirm";

export default function  useConfirmModal(overlay: any) {
    const confirmDialog = overlay.create(ModalConfirm)

    const open = (confirm: ConfirmDialog, callBack: () => void) => {
        confirmDialog.open({
            data: confirm,
            onClose: (isConfirm: boolean) => {
                if (isConfirm)
                    callBack()
            }
        })
    }

    const close = () => {
        confirmDialog.close()
    }

    return { open, close }
}