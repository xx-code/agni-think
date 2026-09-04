<script setup lang="ts">
import { UKbd } from '#components';
import { useTagModal } from '~/composables/modal/tag';
import useConfirmModal from '~/composables/modal/useConfirmModal';
import { deletedResponseToDeleted } from '~/mappers';
import { listTagsResponseToListTags } from '~/mappers/tag';
import { API_ROUTES } from '~/shared/routes';
import type { DeletedResponse, ListResponse } from '~/types/api';
import type { ArchiveTagRequest, GetTagResponse } from '~/types/api/tag';

const overlay = useOverlay();
const { open } = useTagModal(overlay)
const { open: openConfirm } = useConfirmModal(overlay)

const { data: tags, refresh: refreshTags } = useAsyncData('settings+tags', async () => {
    const res = await ApiLinkBuilder.route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS).query({ queryAll: true, limit: 0, offset: 0}).mapper(listTagsResponseToListTags).execute()

    return res.items
})

function onDelete(id: string, title: string) {
    openConfirm({
        title: `Voulez vous supprimer ${title}?`,
        description: ''
    }, async () => {
        const res = await ApiLinkBuilder
            .route<DeletedResponse>(API_ROUTES.TAGS.DELETE_TAG)
            .mapper(deletedResponseToDeleted)
            .params({ id })
            .execute()
        refreshTags()
        
        if (res.inUse) {
            openConfirm({
                title: "Cette item est utilise allieurs vous pouvez l'archiver",
                description: ""
            }, async () => {
                await ApiLinkBuilder
                    .route(API_ROUTES.TAGS.ARCHIVE_TAG)
                    .body({
                        archive: true
                    } as ArchiveTagRequest)
                    .params({ id })
                    .execute()

                refreshTags()
            })
        } 
    })

}

async function unArchive(id: string) {
    await ApiLinkBuilder
        .route(API_ROUTES.TAGS.ARCHIVE_TAG)
        .body({
            archive: false
        } as ArchiveTagRequest)
        .params({ id })
        .execute()
    
    refreshTags()
}

</script>

<template>
    <UiCard>
        <div class="space-y-6">
            <div class="flex items-end justify-between">
                <div>
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Tags</h2>
                    <p class="text-sm text-gray-500">Classification secondaire</p>
                </div>
                <UButton 
                    label="Ajouter tag" 
                    icon="i-lucide-plus" 
                    size="md"
                    @click="open(refreshTags)"
                />
            </div>

            <div class="flex flex-wrap gap-3">
                <div v-for="tag of tags" :key="tag.id">
                    <div 
                        :class="[
                            'bg-white flex items-center gap-2 border rounded-xl px-3 py-1'
                        ]" 
                        :style="{color: tag.color }">
                        <p class="text-sm text-gray-500">
                            {{ tag.value }}
                        </p>

                        <div class="text-right" v-if="!tag.isSystem && !tag.isArchived">
                            <UButton 
                                variant="ghost" 
                                color="info" 
                                size="sm"
                                icon="i-lucide-pencil" 
                                @click="open(refreshTags, tag.id)" />
                            <UButton  
                                variant="ghost" 
                                color="error" 
                                size="sm"
                                icon="i-lucide-trash-2" 
                                @click="onDelete(tag.id, tag.value)"
                             />
                        </div> 

                        <div v-if="!tag.isSystem && tag.isArchived">
                            <UButton 
                                variant="ghost" 
                                color="neutral" 
                                icon="i-lucide-archive-restore" 
                                size="xs"
                                @click="unArchive(tag.id)" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </UiCard>
</template>