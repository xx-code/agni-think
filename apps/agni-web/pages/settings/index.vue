<script setup lang="ts">
import { useFetchCategories } from "../../composables/categories";
import { useFetchTags } from "../../composables/tags";


const categories = useFetchCategories()
const tags = useFetchTags()

</script>

<template>
    <div>
        <div style="margin-top: 1rem;">
            <div class="flex justify-between items-center mb-3">
                <h3 class="font-semibold">Categories</h3>
                <UButton label="Ajouter categorie" icon="i-lucide-plus" />
            </div>
            <USeparator class="mb-3"/>
            <div class="flex gap-1">
                <div v-for="category of categories" :key="category.id">
                    <div class="flex items-center gap-2 border-1 rounded-md p-1" :style="'color:'+category.color+';'">
                        <UIcon :name="category.icon" />
                        {{ category.title }}
                        <UButton variant="outline" color="neutral" icon="i-lucide-pencil" />
                        <UButton  variant="outline" color="neutral" icon="i-lucide-trash-2" />
                    </div>
                </div>
            </div>
        </div>
        
        <div style="margin-top: 1rem;">
            <div class="flex justify-between items-center mb-3">
                <h3 class="font-semibold">Tags</h3>
                <UButton label="Ajouter tag" icon="i-lucide-plus"/>
            </div>
            <USeparator class="mb-3" />
            <div class="flex gap-1">
                <div v-for="tag of tags" :key="tag.id">
                    <div class="flex items-center gap-2 border-1 rounded-md p-1" :style="'color:'+tag.color+';'">
                        {{ tag.value }}
                        <UButton variant="outline" color="neutral" icon="i-lucide-pencil" />
                        <UButton  variant="outline" color="neutral" icon="i-lucide-trash-2" />
                    </div>
                </div>
            </div>
        </div>
        
    </div>    
</template>
