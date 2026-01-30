<script setup lang="ts">
import { ModalEditCategory, ModalEditTag } from "#components";
import useCategories from "~/composables/categories/useCategories";
import { fetchCategory } from "~/composables/categories/useCategory";
import useCreateCategory from "~/composables/categories/useCreateCategory";
import useUpdateCategory from "~/composables/categories/useUpdateCategory";
import useCreateTag from "~/composables/tags/useCreateTag";
import { fetchTag } from "~/composables/tags/useTag";
import useTags from "~/composables/tags/useTags";
import useUpdateTag from "~/composables/tags/useUpdateTag";
import type { CategoryType, EditCategoryType } from "~/types/ui/category";
import type { EditTagType, TagType } from "~/types/ui/tag";

const {data: categories, error: errorCategories, refresh: refreshCategories} = useCategories({
    limit: 0, offset: 0, queryAll: true
});
const {data: tags, error: errorTags, refresh: refresTags } = useTags({
    limit: 0, offset: 0, queryAll: true
});

const overlay = useOverlay();
const modalCategory = overlay.create(ModalEditCategory);
const modalTag = overlay.create(ModalEditTag);
const toast = useToast();

async function onSubmitCategory(value: EditCategoryType, oldValue?: CategoryType) {
    try {
        if(oldValue) {
            await useUpdateCategory(oldValue.id, {
                title: value.title,
                icon: value.icon,
                color: value.color
            });
        } else {
            await useCreateCategory({
                title: value.title,
                icon: value.icon,
                color: value.color
            });
        }
        refreshCategories();
    } catch(err) {
        toast.add({
            title: "Error Category",
            description:`Error while submit category`, 
            color: 'error'
        });
    }
}

async function onSubmitTag(value: EditTagType, oldValue?: TagType) {
    try {
        if(oldValue) {
            await useUpdateTag(oldValue.id, {
                value: value.value,
                color: value.color
            });
        } else {
            await useCreateTag({
                value: value.value,
                color: value.color
            });
        }
        refresTags();
    } catch(err) {
        toast.add({
            title: "Error tag",
            description:`Error while submit tag`, 
            color: 'error'
        });
    }
}

const openModalCategory = async (categoryId?: string) => {  
    let category:CategoryType|undefined=undefined;
    if (categoryId) {
        category = await fetchCategory(categoryId); 
    }

    modalCategory.open({
        category: category,
        onSubmit: onSubmitCategory
    });
}

const openModalTag = async (tagId?: string) => {  
    let tag:TagType|undefined=undefined;
    if (tagId) {
        tag = await fetchTag(tagId); 
    }
    modalTag.open({
        tag: tag,
        onSubmit: onSubmitTag
    });
}

</script>

<template>
    <div class="p-5 space-y-4">
        <UCard>
            <div>
                <div>
                    <p class="font-bold text-2xl">Général</p>
                    <p class="text-xs text-gray-500">Configuration globale de l’application</p>
                </div>
            </div>
        </UCard>

        <UCard>
            <div>
                <div>
                    <p class="font-bold text-2xl">Deduction</p>
                    <p class="text-xs text-gray-500">Gestion des decution et taxe</p>
                </div>
            </div>
        </UCard>

        <UCard>
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
                        @click="openModalCategory()"
                    />
                </div>

                <div class="max-w-md">
                    <UInput 
                        icon="i-lucide-search" 
                        placeholder="Rechercher une catégorie..." 
                        size="md"
                    />
                </div>

                <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
                    <div v-for="category of categories?.items" 
                        :key="category.id"
                        class="group relative flex flex-col p-4 bg-white border border-gray-100 rounded-2xl shadow-sm transition-all duration-200 hover:shadow-md hover:-translate-y-1">
                        <div class="absolute inset-0 opacity-[0.03] rounded-2xl pointer-events-none" 
                            :style="{ backgroundColor: category.color }">
                        </div>

                        <div class="flex items-center justify-between mb-3">
                            <div 
                                class="flex items-center justify-center w-8 h-8 rounded-xl"
                                :style="{ backgroundColor: category.color + '20' }" >
                                <UIcon 
                                    :name="category.icon" 
                                    class="w-6 h-6"
                                    :style="{ color: category.color }"
                                />
                            </div>

                            <div class="flex gap-1 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                                <UButton 
                                    variant="ghost" 
                                    color="neutral" 
                                    icon="i-lucide-pencil" 
                                    size="xs"
                                    @click="openModalCategory(category.id)" />
                                <UButton 
                                    variant="ghost" 
                                    color="error" 
                                    icon="i-lucide-trash-2" 
                                    size="xs" />
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
        </UCard>

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
                    <div v-for="tag of tags?.items" :key="tag.id">
                        <div 
                            class="bg-white flex items-center gap-2 border-1 rounded-full px-3 py-1" 
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
    </div>    
</template>
