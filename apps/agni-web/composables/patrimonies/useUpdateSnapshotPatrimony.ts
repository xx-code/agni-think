import type { UpdateSnapshotPatrimonyRequest } from "~/types/api/patrimony";

export async function useUpdateSnapshotPatrimony(patrimonyId: string, snapshotId: string, 
    request: UpdateSnapshotPatrimonyRequest): Promise<void> {
    await $fetch(`/api/patrimonies/${patrimonyId}/update-snapshot/${snapshotId}`, {
        method: 'PUT',
        body: request
    })
}