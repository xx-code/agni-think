export default async function useDeleteTag(tagId: string): Promise<void> {
    await $fetch(`/api/tags/${tagId}`, {
        method: 'DELETE',
    });
}