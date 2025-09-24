export async function useDeletePatrimony(patrimonyId: string): Promise<void> {
    await $fetch(`/api/patrimonies/${patrimonyId}`, {
        method: 'DELETE'
    })
} 