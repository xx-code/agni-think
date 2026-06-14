<script lang="ts" setup>
import type { EditTransactionType } from '~/types/form/invoice';
import type { BudgetType } from '~/types/ui/budget';
import type { CategoryType } from '~/types/ui/category';
import type { TagType } from '~/types/ui/tag';

const { categories, budgets, tags } = defineProps<{
    categories: CategoryType[]
    budgets: BudgetType[]
    tags: TagType[]
}>()

const model = defineModel<{
    transactions?: EditTransactionType[]
}>()

const hasMoreTransaction = computed(() => {
    if (!model.value) return false

    return model.value.transactions && model.value.transactions.length > 0 || false
})

function addTransaction() {
    if (!model.value) return

    if (!model.value.transactions) model.value.transactions = [];
    model.value.transactions.push({
        amount: 0,
        description: '',
        categoryId: '',
        tagIds: [],
        budgetIds: []
    });
}

function removeTransaction(index: number) {
    if (!model.value) return

    if (model.value.transactions && model.value.transactions.length > 1) {
        model.value.transactions.splice(index, 1);
    }
}

function duplicateTransaction(index: number) {
    if (!model.value) return

    if (model.value.transactions && model.value.transactions[index]) {
        const transactionToDuplicate = model.value.transactions[index];
        model.value.transactions.push({
            amount: transactionToDuplicate.amount || 0,
            description: `${transactionToDuplicate.description} (copie)`,
            categoryId: transactionToDuplicate.categoryId || '',
            tagIds: transactionToDuplicate.tagIds || [],
            budgetIds: transactionToDuplicate.budgetIds || []
        });
    }
}

</script>

<template>
    <div class="space-y-4" v-if="model">
        <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
                Articles ({{ model.transactions?.length || 0 }})
            </h3>
            <UButton 
                icon="i-lucide-plus" 
                label="Ajouter un article"
                color="primary"
                variant="soft"
                @click="addTransaction"
            />
        </div>

        <div class="space-y-4"  v-if="model.transactions">
            <FormInvoiceTransactionCard 
                v-for="(transaction, index) in model.transactions"
                :key="index"
                :index="index"
                :budgets="budgets"
                :categories="categories"
                :tags="tags"
                :has-more-transactions="hasMoreTransaction"

                @remove="removeTransaction(index)"
                @duplicate="duplicateTransaction(index)"

                v-model="model.transactions[index]"
            />
        </div>
    </div>
</template>