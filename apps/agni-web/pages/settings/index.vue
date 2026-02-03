<script setup lang="ts">
import { ModalEditCategory, ModalEditDeductionType, ModalEditTag } from "#components";
import  { fetchCategories } from "~/composables/categories/useCategories";
import { fetchCategory } from "~/composables/categories/useCategory";
import useCreateCategory from "~/composables/categories/useCreateCategory";
import useUpdateCategory from "~/composables/categories/useUpdateCategory";
import useCreateTag from "~/composables/tags/useCreateTag";
import { fetchTag } from "~/composables/tags/useTag";
import { fetchTags } from "~/composables/tags/useTags";
import useUpdateTag from "~/composables/tags/useUpdateTag";
import type { CategoryType, EditCategoryType } from "~/types/ui/category";
import type { DeductionTypeType, EditDeductionType } from "~/types/ui/deduction";
import type { EditTagType, TagType } from "~/types/ui/tag";

const { data: tags, error: errorTag, refresh: refreshTags } = useAsyncData('settings+tags', async () => {
    const res = await fetchTags({ queryAll: true, limit: 0, offset: 0}) 

    return res.items
})
const { data: categories, error: errorCategory, refresh: refreshCategories } = useAsyncData('settings+categories', async () => {
    const res = await fetchCategories({ queryAll: true, limit: 0, offset: 0}) 

    return res.items
})
const { data: deductionTypes, error: errorDeductionType, refresh: refreshDeductionTypes } = useAsyncData('settings+deduction-types', async () => {
    const res = await fetchDeductionTypes({ queryAll: true, limit: 0, offset: 0})

    return res.items
})


const overlay = useOverlay();
const modalCategory = overlay.create(ModalEditCategory);
const modalTag = overlay.create(ModalEditTag);
const modalEditDeductionType = overlay.create(ModalEditDeductionType);
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
        refreshTags();
    } catch(err) {
        toast.add({
            title: "Error tag",
            description:`Error while submit tag`, 
            color: 'error'
        });
    }
}

async function onSubmitDeductionType(value: EditDeductionType, oldValue?: DeductionTypeType) {
    try {
        if(oldValue) {
            await updateDeductionType(oldValue.id, {
                id: oldValue.id, 
                description: value.description,
                title: value.title
            });
        } else {
            await createDeductionType({
                title: value.title,
                description: value.description,
                mode: value.mode,
                base: value.base
            });
        }
        await refreshDeductionTypes();
    } catch(err) {
        toast.add({
            title: "Error Deduction type",
            description:`Error while submit deduction type`, 
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

const openModalDeductionType = async (id?: string) => {  
    let type:DeductionTypeType|undefined=undefined;
    if (id) {
        type = await fetchDeductionType(id); 
    }

    modalEditDeductionType.open({
        deductionType: type, 
        onSubmit: onSubmitDeductionType
    });
}


const onDeleteDeductionType = async (id: string) => {
    try {
        await deleteDeductionType(id) 
        refreshDeductionTypes()
    } catch(err) {
        toast.add({
            title: "Error delete deduction type",
            description:`Error while delete deduction type`, 
            color: 'error'
        });       
    }
}

</script>

<template>
    <div class="p-5 space-y-4">
        <UCard>
            <div>
                <div>
                    <p class="font-bold text-2xl">Général</p>
                    <p class="text-xs text-gray-500">Configuration globale de l'application</p>
                </div>
            </div>
        </UCard>

        <!-- Deductions Section -->
        <UCard>
            <div class="space-y-6">
                <div class="flex items-end justify-between">
                    <div>
                        <h2 class="text-2xl font-bold tracking-tight text-gray-900">Déductions & Taxes</h2>
                        <p class="text-sm text-gray-500">Gestion des déductions actives et de leurs taux</p>
                    </div>
                    <UButton 
                        label="Nouvelle déduction" 
                        icon="i-lucide-plus" 
                        size="md"
                        color="primary"
                        @click="openModalDeductionType()"
                    />
                </div>

                <div class="max-w-md">
                    <UInput 
                        icon="i-lucide-search" 
                        placeholder="Rechercher une déduction..." 
                        size="md"
                    />
                </div>

                <!-- Table view for deductions -->
                <div class="overflow-hidden border border-gray-200 rounded-xl">
                    <table class="min-w-full divide-y divide-gray-200">
                        <thead class="bg-gray-50">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Titre
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Description
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Base
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Mode
                                </th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <tr v-for="deduction of deductionTypes" 
                                :key="deduction.id"
                                class="hover:bg-gray-50 transition-colors">
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="flex items-center gap-3">
                                        <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-purple-50">
                                            <UIcon 
                                                name="i-lucide-percent" 
                                                class="w-4 h-4 text-purple-600"
                                            />
                                        </div>
                                        <span class="text-sm font-medium text-gray-900">
                                            {{ deduction.title }}
                                        </span>
                                    </div>
                                </td>

                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                        {{ deduction.description }}
                                    </span>
                                </td>

                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                        {{ deduction.base }}
                                    </span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                        {{ deduction.mode }}
                                    </span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-right">
                                    <div class="flex items-center justify-end gap-2">
                                        <UButton 
                                            variant="ghost" 
                                            color="neutral" 
                                            icon="i-lucide-pencil" 
                                            size="xs"
                                            @click="openModalDeductionType(deduction.id)" 
                                        />
                                        <UButton 
                                            variant="ghost" 
                                            color="error" 
                                            icon="i-lucide-trash-2" 
                                            size="xs" 
                                            @click="onDeleteDeductionType(deduction.id)"
                                        />
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Empty state -->
                <div v-if="!deductionTypes || deductionTypes.length === 0" 
                    class="text-center py-12 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
                    <UIcon name="i-lucide-percent" class="w-12 h-12 text-gray-300 mx-auto mb-3" />
                    <p class="text-gray-500 text-sm">Aucune déduction configurée</p>
                    <UButton 
                        label="Créer la première déduction" 
                        size="sm" 
                        class="mt-3"
                        @click="openModalDeductionType()"
                    />
                </div>
            </div>
        </UCard>

        <!-- Categories Section -->
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
                    <div v-for="category of categories" 
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

        <!-- Tags Section -->
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