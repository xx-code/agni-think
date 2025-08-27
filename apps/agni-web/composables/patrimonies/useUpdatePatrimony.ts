import type { UpdatePatrimonyRequest, UpdateSnapshotPatrimonyRequest } from "~/types/api/patrimony";

export async function useUpdatePatrimony(patrimonyId: string, 
    request: UpdatePatrimonyRequest): Promise<void> {
    await $fetch(`/api/patrimonies/${patrimonyId}`, {
        method: 'PUT',
        body: request
    })
}