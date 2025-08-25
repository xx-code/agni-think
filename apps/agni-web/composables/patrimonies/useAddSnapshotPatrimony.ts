import type { AddSnapshotPatrimonyRequest } from "~/types/api/patrimony";

export async  function useAddSnapshotPatrimony(snapshotId: string, request: AddSnapshotPatrimonyRequest) {
    await $fetch(`/api/patrimonies/${snapshotId}/add-snapshots`, {
        method: "POST",
        body: request
    })
}