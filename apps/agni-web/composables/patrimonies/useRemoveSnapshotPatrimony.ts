export async function useRemoveSnapshotPatrimony(snapshotId: string): Promise<void> {
    await $fetch(`/api/delete-snapshot/${snapshotId}`);
}