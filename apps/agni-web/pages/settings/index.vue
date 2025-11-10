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
    <div>
        <div style="margin-top: 1rem;">
            <div class="flex justify-between items-center mb-3">
                <h3 class="font-semibold">Categories</h3>
                <UButton label="Ajouter categorie" icon="i-lucide-plus" @click="openModalCategory()"/>
            </div>
            <USeparator class="mb-3"/>
            <div class="flex flex-wrap gap-3">
                <div v-for="category of categories?.items" :key="category.id">
                    <div 
                        class="bg-white flex items-center gap-2 border-1 rounded-md p-1" 
                        :style="'color:'+category.color+';'">
                        <UIcon :name="category.icon" />
                        {{ category.title }}
                        <UButton 
                            variant="outline" 
                            color="neutral" 
                            icon="i-lucide-pencil" 
                            @click="openModalCategory(category.id)" />
                        <UButton  
                            variant="outline" 
                            color="neutral" 
                            icon="i-lucide-trash-2" />
                    </div>
                </div>
            </div>
        </div>
        
        <div style="margin-top: 1rem;">
            <div class="flex justify-between items-center mb-3">
                <h3 class="font-semibold">Tags</h3>
                <UButton label="Ajouter tag" icon="i-lucide-plus" @click="openModalTag()"/>
            </div>
            <USeparator class="mb-3" />
            <div class="flex flex-wrap gap-3">
                <div v-for="tag of tags?.items" :key="tag.id">
                    <div 
                        class="bg-white flex items-center gap-2 border-1 rounded-md p-1" 
                        :style="'color:'+tag.color+';'">
                        {{ tag.value }}
                        <UButton 
                            variant="outline" 
                            color="neutral" 
                            icon="i-lucide-pencil" 
                            @click="openModalTag(tag.id)" />
                        <UButton  
                            variant="outline" 
                            color="neutral" 
                            icon="i-lucide-trash-2" />
                    </div>
                </div>
            </div>
        </div>
        
    </div>    
</template>
