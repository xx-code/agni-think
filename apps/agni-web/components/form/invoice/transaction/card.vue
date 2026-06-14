<script lang="ts" setup>
import type { BudgetType } from '~/types/ui/budget';
import type { CategoryType } from '~/types/ui/category';
import type { TagType } from '~/types/ui/tag';

const { categories, budgets, tags, hasMoreTransactions } = defineProps<{
    categories: CategoryType[]
    budgets: BudgetType[]
    tags: TagType[]
    index: number
    hasMoreTransactions: boolean
}>() 

const model = defineModel<{
    amount: number
    categoryId: string
    description: string
    tagIds: string[]
    budgetIds: string[]
}>()

const emit = defineEmits(['remove', 'duplicate'])

</script>

<template>
    <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-4 bg-white dark:bg-gray-900 space-y-4">
        <FormInvoiceTransactionCardHead 
            v-model="model"
            :categories="categories"
            :has-more-transactions="hasMoreTransactions"
            :order="index + 1"

            @remove="emit('remove')"
            @duplicate="emit('duplicate')"
        />
        <FormInvoiceTransactionCardContent 
            v-model="model"
            :budgets="budgets"  
            :categories="categories"
            :tags="tags"
            :index="index"
        />
    </div>
</template>