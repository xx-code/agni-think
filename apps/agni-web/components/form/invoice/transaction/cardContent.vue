<script lang="ts" setup>
import type { BudgetType } from '~/types/ui/budget';
import type { CategoryType } from '~/types/ui/category';
import type { TagType } from '~/types/ui/tag';

const { categories, budgets, tags } = defineProps<{
    categories: CategoryType[]
    budgets: BudgetType[]
    tags: TagType[]
    index: number
}>()
const model = defineModel<{
    amount: number
    categoryId: string
    description: string
    tagIds: string[]
    budgetIds: string[]
}>()
</script>

<template>
    <div v-if="model">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <UFormField 
                :label="`Montant`" 
                :name="`transactions[${index}].amount`"
                required
            >
                <UInput 
                    v-model="model.amount" 
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    icon="i-lucide-dollar-sign"
                />
            </UFormField>

            <UFormField 
                :label="`Catégorie`" 
                :name="`transactions[${index}].categoryId`"
                required
            >
                <USelectMenu 
                    v-model="model.categoryId" 
                    value-key="value"
                    :items="categories.map(i => ({ 
                        value: i.id, 
                        label: i.title,
                        icon: i.icon,
                        color: i.color
                    }))"
                    placeholder="Sélectionner une catégorie"
                >
                    <template #item="{ item }">
                        <div class="flex items-center gap-2">
                            <div 
                                class="flex items-center justify-center rounded-full"
                                :style="{
                                    background: `${item.color}22`,
                                    width: '24px',
                                    height: '24px',
                                }"
                            >
                                <UIcon 
                                    :name="item.icon" 
                                    class="text-sm"
                                    :style="{ color: item.color }" 
                                />
                            </div>
                            <span>{{ item.label }}</span>
                        </div>
                    </template>
                </USelectMenu>
            </UFormField>
        </div>

        <UFormField 
            :label="`Description`" 
            :name="`transactions[${index}].description`"
            required
        >
            <UInput 
                v-model="model.description"
                placeholder="Ex: Épicerie du mois"
            />
        </UFormField>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <UFormField :label="`Tags`" :name="`records[${index}].tagIds`">
                <UInputMenu 
                    v-model="model.tagIds" 
                    multiple 
                    value-key="value"
                    :items="tags.map(i => ({ 
                        value: i.id, 
                        label: i.value,
                        color: i.color 
                    }))"
                    placeholder="Sélectionner des tags"
                >
                    <template #item="{ item }">
                        <UBadge 
                            :label="item.label"
                            :style="{ borderColor: item.color, color: item.color }"
                            variant="outline"
                            size="sm"
                        />
                    </template>
                </UInputMenu>
            </UFormField>

            <UFormField :label="`Budgets`" :name="`transactions[${index}].budgetIds`">
                <UInputMenu 
                    v-model="model.budgetIds" 
                    multiple 
                    value-key="value" 
                    :items="budgets.map(i => ({ value: i.id, label: i.title }))"
                    placeholder="Sélectionner des budgets"
                >
                    <template #item="{ item }">
                        <div class="flex items-center gap-2">
                            <UIcon name="i-lucide-pie-chart" class="text-primary-500" />
                            <span>{{ item.label }}</span>
                        </div>
                    </template>
                </UInputMenu>
            </UFormField>
        </div>

    </div> 
</template>