<script setup lang="ts">
import { UKbd } from '#components';
import useCategoryModal from '~/composables/modal/useCategoryModal';
import useConfirmModal from '~/composables/modal/useConfirmModal';
import { deletedResponseToDeleted } from '~/mappers';
import { listCategoriesResponseToListCategories } from '~/mappers/category';
import { API_ROUTES } from '~/shared/routes';
import type { DeletedResponse, ListResponse } from '~/types/api';
import type { ArchiveCategoryRequest, GetCategoryResponse } from '~/types/api/category';
import type { Deleted } from '~/types/ui';

const overlay = useOverlay()

const { open } = useCategoryModal(overlay)
const { open: openConfirm } = useConfirmModal(overlay)

const { data: categories, refresh: refreshCategories } = useAsyncData('settings+categories', async () => {
    const res = await ApiLinkBuilder
        .route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES)
        .query({ queryAll: true, limit: 0, offset: 0})
        .mapper(listCategoriesResponseToListCategories).execute()

    return res.items
})

function onDelete(id: string, title: string) {
    openConfirm({
        title: `Voulez vous supprimer ${title}?`,
        description: ''
    }, async () => {
        const res = await ApiLinkBuilder
            .route<DeletedResponse>(API_ROUTES.CATEGORIES.DELETE_CATEGORY)
            .mapper(deletedResponseToDeleted)
            .params({ id })
            .execute()
         refreshCategories()
        
        if (res.inUse) {
            openConfirm({
                title: "Cette item est utilise allieurs vous pouvez l'archiver",
                description: ""
            }, async () => {
                await ApiLinkBuilder
                    .route(API_ROUTES.CATEGORIES.ARCHIVE_CATEGORY)
                    .body({
                        archive: true
                    } as ArchiveCategoryRequest)
                    .params({ id })
                    .execute()

                refreshCategories()
            })
        } 
    })

}

async function unArchive(id: string) {
    await ApiLinkBuilder
        .route(API_ROUTES.CATEGORIES.ARCHIVE_CATEGORY)
        .body({
            archive: false
        } as ArchiveCategoryRequest)
        .params({ id })
        .execute()
    
    refreshCategories()
}

</script>

<template>
    <UiCard>
        <div class="space-y-6">
            <div class="flex items-end justify-between">
                <div>
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Catégories</h2>
                    <p class="text-sm text-gray-500">Gérez vos enveloppes budgétaires</p>
                </div>
                <UButton 
                    label="Nouvelle catégorie" 
                    icon="i-lucide-plus" 
                    size="md"
                    @click="open(refreshCategories)"
                />
            </div>

            <!-- <div class="max-w-md">
                <UInput 
                    icon="i-lucide-search" 
                    placeholder="Rechercher une catégorie..." 
                    size="md"
                />
            </div> -->

            <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
                <div v-for="category of categories" 
                    :key="category.id"
                    class="group relative flex flex-col p-4 bg-white border border-gray-100 rounded-2xl shadow-sm transition-all duration-200 hover:shadow-md hover:-translate-y-1">

                    <div v-if="category.isArchive" class="flex items-center justify-end">
                        <UKbd color="warning">Archiver</UKbd>
                    </div>

                    <div class="absolute inset-0 opacity-[0.03] rounded-2xl pointer-events-none" 
                        :style="{ backgroundColor: category.color }">
                    </div>

                    <div class="flex items-center justify-between mb-3">
                        <div 
                            class="flex items-center justify-center w-8 h-8 rounded-xl"
                            :style="{ backgroundColor: category.color + '30' }" >
                            <UIcon 
                                :name="category.icon" 
                                class="w-6 h-6"
                                :style="{ color: category.color }"
                            />
                        </div>
                        
                        <div 
                            v-if="!category.isSystem && !category.isArchive"
                            class="flex gap-1 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                            <UButton 
                                variant="ghost" 
                                color="neutral" 
                                icon="i-lucide-pencil" 
                                size="xs"
                                @click="open(refreshCategories, category.id)" />
                            <UButton 
                                variant="ghost" 
                                color="error" 
                                icon="i-lucide-trash-2" 
                                size="xs" 
                                @click="onDelete(category.id, category.title)"/>
                        </div>

                        <div v-else-if="!category.isSystem && category.isArchive">
                            <UButton 
                                variant="ghost" 
                                color="neutral" 
                                icon="i-lucide-archive-restore" 
                                size="xs"
                                @click="unArchive(category.id)" />
                        </div>
                    </div>

                    <div>
                        <h3 class="font-bold text-gray-800 capitalize">{{ category.title }}</h3>
                        <div 
                            class="mt-2 h-1.5 w-12 rounded-full"
                            :style="{ backgroundColor: category.color }">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </UiCard>
</template>