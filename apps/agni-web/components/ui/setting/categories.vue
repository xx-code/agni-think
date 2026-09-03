<script setup lang="ts">
import { ModalEditCategory } from '#components';
import { categoryResponseToCategory, listCategoriesResponseToListCategories } from '~/mappers/category';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest, ListResponse } from '~/types/api';
import type { GetCategoryResponse } from '~/types/api/category';
import type { CategoryType, EditCategoryType } from '~/types/ui/category';

const toast = useToast()
const overlay = useOverlay()
const modalCategory = overlay.create(ModalEditCategory);

const { data: categories, refresh: refreshCategories } = useAsyncData('settings+categories', async () => {
    const res = await ApiLinkBuilder
        .route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES)
        .query({ queryAll: true, limit: 0, offset: 0})
        .mapper(listCategoriesResponseToListCategories).execute()

    return res.items
})

async function onSubmitCategory(value: EditCategoryType, oldValue?: CategoryType) {
    try {
        if(oldValue) {
            await ApiLinkBuilder.route(API_ROUTES.CATEGORIES.UPDATE_CATEGORY).params({id: oldValue.id}).body({
                title: value.title,
                icon: value.icon,
                color: value.color
            }).execute();
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.CATEGORIES.CREATE_CATEGORY).body({
                title: value.title,
                icon: value.icon,
                color: value.color
            }).execute();
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

const openModalCategory = async (categoryId?: string) => {  
    let category:CategoryType|undefined;
    if (categoryId) {
        category = await ApiLinkBuilder.route<GetCategoryResponse>(API_ROUTES.CATEGORIES.GET_CATEGORY).params({id: categoryId}).mapper(categoryResponseToCategory).execute(); 
    }

    modalCategory.open({
        category: category,
        onSubmit: onSubmitCategory
    });
}


</script>

<template>
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
</template>