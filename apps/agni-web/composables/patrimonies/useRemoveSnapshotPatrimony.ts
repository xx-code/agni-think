export async function useRemoveSnapshotPatrimony(patrimonyId: string, snapshotId: string): Promise<void> {
    await $fetch(`/api/patrimonies/${patrimonyId}/snapshots/${snapshotId}`, {
        method: 'DELETE'
    });
}