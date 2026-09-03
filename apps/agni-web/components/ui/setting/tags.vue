<script setup lang="ts">
import { ModalEditTag } from '#components';
import { tagResponseToTag, listTagsResponseToListTags } from '~/mappers/tag';
import { API_ROUTES } from '~/shared/routes';
import type { ListResponse, CreatedRequest } from '~/types/api';
import type { GetTagResponse } from '~/types/api/tag';
import type { TagType, EditTagType } from '~/types/ui/tag';


const overlay = useOverlay();
const modalTag = overlay.create(ModalEditTag);
const toast = useToast();

const openModalTag = async (tagId?: string) => {  
    let tag:TagType|undefined=undefined;
    if (tagId) {
        tag = await ApiLinkBuilder.route<GetTagResponse>(API_ROUTES.TAGS.GET_TAG).params({id: tagId}).mapper(tagResponseToTag).execute(); 
    }
    modalTag.open({
        tag: tag,
        onSubmit: onSubmitTag
    });
}

const { data: tags, error: errorTag, refresh: refreshTags } = useAsyncData('settings+tags', async () => {
    const res = await ApiLinkBuilder.route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS).query({ queryAll: true, limit: 0, offset: 0}).mapper(listTagsResponseToListTags).execute()

    return res.items
})


async function onSubmitTag(value: EditTagType, oldValue?: TagType) {
    try {
        if(oldValue) {
            await ApiLinkBuilder.route(API_ROUTES.TAGS.UPDATE_TAG).params({id: oldValue.id}).body({
                value: value.value,
                color: value.color
            }).execute();
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.TAGS.CREATE_TAG).body({
                value: value.value,
                color: value.color
            }).execute();
        }
        refreshTags();
    } catch(err) {
        toast.add({
            title: "Error tag",
            description:`Error while submit tag`, 
            color: 'error'
        });
    }
}
</script>

<template>
    <UCard>
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
                    @click="openModalTag()"
                />
            </div>

            <div class="max-w-md">
                <UInput 
                    icon="i-lucide-search" 
                    placeholder="Rechercher un tag..." 
                    size="md"
                />
            </div>

            <div class="flex flex-wrap gap-3">
                <div v-for="tag of tags" :key="tag.id">
                    <div 
                        class="bg-white flex items-center gap-2 border rounded-full px-3 py-1" 
                        :style="'color:'+tag.color+';'">
                        <p class="text-sm text-gray-500">
                            {{ tag.value }}
                        </p>

                        <div class="text-right">
                            <UButton 
                                variant="ghost" 
                                color="info" 
                                size="sm"
                                icon="i-lucide-pencil" 
                                @click="openModalTag(tag.id)" />
                            <UButton  
                                variant="ghost" 
                                color="error" 
                                size="sm"
                                icon="i-lucide-trash-2" />
                        </div> 
                    </div>
                </div>
            </div>
        </div>
    </UCard>
</template>