import type { AddSnapshotPatrimonyRequest } from "~/types/api/patrimony";

export async  function useAddSnapshotPatrimony(patrimonyId: string, request: AddSnapshotPatrimonyRequest) {
    await $fetch(`/api/patrimonies/${patrimonyId}/add-snapshot`, {
        method: "POST",
        body: request
    })
}