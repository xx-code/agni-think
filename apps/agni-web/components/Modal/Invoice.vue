<script setup lang="ts">
import { reactive } from "vue";
import type { InvoiceType } from '~/types/ui/transaction';
import type { FormError, FormSubmitEvent } from '#ui/types';
import type { NuxtError } from "#app";
import type { CreatedRequest, ErrorResponse, ListResponse } from '~/types/api';
import type { GetCategoryResponse } from '~/types/api/category';
import type { GetTagResponse } from '~/types/api/tag';
import type { GetAccountResponse } from '~/types/api/account';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import type { GetDeductionResponse } from '~/types/api/deduction';
import { listCategoriesResponseToListCategories } from '~/mappers/category';
import { listTagsResponseToListTags } from '~/mappers/tag';
import { budgetFilterToBudgetQueryRequest, listBudgetsResponseToListBudgets } from '~/mappers/budget';
import { listDeductionsResponseToListDeductions } from '~/mappers/deduction';
import { listAccountsToListAccount } from '~/mappers/account';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import type { EditInvoiceType } from '~/types/form/invoice';

const { invoice, accountSelectedId } = defineProps<{
    invoice?: InvoiceType
    accountSelectedId?: string
}>();

const emit = defineEmits<{
    (e: 'close', doRefresh: boolean): void
}>();

const toast = useToast()
const switchMoreState = ref(false)
const isLoading = ref(false)

const { data: utils } = useAsyncData('utils+deduction+edit-invoices', async () => {
    isLoading.value = true
    const query = {offset: 0, limit: 0, queryAll: true, isSystem: false}
    const [ categories, tags, budgets, accounts, invoiceTypes, deductions ] = await Promise.all([
        ApiLinkBuilder
            .route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES)
            .query(query)
            .mapper(listCategoriesResponseToListCategories)
            .execute(),
        ApiLinkBuilder
            .route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS)
            .query(query)
            .mapper(listTagsResponseToListTags)
            .execute(),
        ApiLinkBuilder
            .route(API_ROUTES.BUDGETS.GET_BUDGETS)
            .query(budgetFilterToBudgetQueryRequest({offset: 0, limit: 10, queryAll: true}))
            .mapper(listBudgetsResponseToListBudgets)
            .execute(),
        ApiLinkBuilder
            .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
            .query(query)
            .mapper(listAccountsToListAccount)
            .execute(),
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.TRANSACTION_TYPE).execute(),
        ApiLinkBuilder
            .route<ListResponse<GetDeductionResponse>>(API_ROUTES.DEDUCTIONS.GET_DEDUCTIONS)
            .query(query)
            .mapper(listDeductionsResponseToListDeductions)
            .execute()
    ])

    isLoading.value = false
    return {
        categories: categories.items,
        tags: tags.items,
        budgets: budgets.items,
        accounts: accounts.items,
        invoiceTypes: invoiceTypes,
        deductions: deductions.items
    }
})


const form = reactive<Partial<EditInvoiceType>>({
    accountId: invoice?.accountId || (accountSelectedId === "ALL_ACCOUNT_ID" ? '' : accountSelectedId),
    state: (invoice?.status as 'Complete' | 'Pending') || 'Pending',
    mouvement: invoice?.mouvement || 'Debit',
    type: invoice?.type || '',
    transactions: invoice?.transactions.map(r => ({
        amount: r.amount,
        description: r.description,
        categoryId: r.category.id,
        tagIds: r.tags.map(i => i.id) || [],
        budgetIds: r.budgets.map(i => i.id) || []
    })) || [{
        amount: 0,
        description: '',
        categoryId: '',
        tagIds: [],
        budgetIds: []
    }],
    deductions: invoice?.deductions.map(d => ({
        deductionId: d.id,
        amount: d.amount
    })) || [],
    date: invoice?.date.toISOString() 
});

function validate(state: Partial<EditInvoiceType>): FormError[] {
    const errors = []
    if (!state.accountId) errors.push({ name: 'accountId', message: 'Required' })
    if (!state.state) errors.push({ name: 'state', message: 'Required' })
    if (!state.mouvement) errors.push({ name: 'mouvement', message: 'Vous devez selectionner un mouvement' })
    if (!state.type) errors.push({ name: 'type', message: 'Vous devez sélectionner un type' })

    if (!state.transactions) {
        errors.push({ name: 'transaction', message: 'Aucune Transactions' })
    } else {
        state.transactions.forEach((transaction, index) => {
            if (!transaction.amount) 
                errors.push({ name: `transactions[${index}].amount`, message: 'required' })
            else {
                if (transaction.amount <= 0)
                    errors.push({ name: `transactions[${index}].amount`, message: 'Le montant doit être supérieur à zéro'})
            }
            
            if (!transaction.categoryId) errors.push({ name: `transactions[${index}].categoryId`, message: 'Vous devez sélectionner une catégorie' })
            if (!transaction.description) errors.push({ name: `transactions[${index}].description`, message: 'La description est requise' }) 
        })
    } 

    if (state.deductions) {
        state.deductions.forEach((deduction, index) => {
            if (!deduction.deductionId) errors.push({ name: `deductions[${index}].deductionId`, message: 'La déduction est requise'})
            if (!deduction.amount) 
                errors.push({ name: `deductions[${index}].amount`, message: 'required'})
            else  {
                if (deduction.amount <= 0)
                    errors.push({ name: `deductions[${index}].account`, message: 'La déduction est requise'})
            }

        })
    }
    
    return errors
}


// Soumission
async function onSubmit(event: FormSubmitEvent<EditInvoiceType>) {
    const data = event.data;
    let isSuccess = false
    let resError: ErrorResponse | undefined = undefined

    if (invoice) {
        const transactionRemovedIds = invoice.transactions.filter(i => !data.transactions.find(
            v => 
                v.amount === i.amount && 
                v.categoryId === i.category.id &&
                v.budgetIds.length === i.budgets.length &&
                v.budgetIds.every(b => i.budgets.map(i => i.id).includes(b)) &&
                v.tagIds.length === i.tags.length &&
                v.tagIds.every(t => i.tags.map(i => i.id).includes(t)) &&
                v.description === i.description
            )).map(i => i.id)

        const transactionAdded = data.transactions.filter(i => !invoice.transactions.find(
            v => 
                v.amount === i.amount && 
                v.category.id === i.categoryId &&
                v.budgets.length === i.budgetIds.length &&
                v.budgets.map(i => i.id).every(b => i.budgetIds.includes(b)) &&
                v.tags.length === i.tagIds.length &&
                v.tags.map(i => i.id).every(t => i.tagIds.includes(t)) &&
                v.description === i.description
            ))

        try {
            await ApiLinkBuilder
                .route(API_ROUTES.INVOICES.UPDATE_INVOICE)
                .params({ id: invoice.id })
                .body({
                    addTransactions: transactionAdded, 
                    mouvement: data.mouvement,
                    removeTransactionIds: transactionRemovedIds,
                    deductions: data.deductions.map(i => ({ deductionId: i.deductionId, amount: i.amount})),
                    accountId: data.accountId,
                    date: data.date,
                    type: data.type
                })
                .execute()

            isSuccess = true
        } catch(err) {
            resError = (err as NuxtError).data as ErrorResponse
        }
    } else {
        try {
            await ApiLinkBuilder
                .route<CreatedRequest>(API_ROUTES.INVOICES.CREATE_INVOICE)
                .body({
                    accountId: data.accountId,
                    date: data.date,
                    mouvement: data.mouvement,
                    type: data.type,
                    status: data.state,
                    transactions: data.transactions.map(i => ({
                        amount: i.amount,
                        categoryId: i.categoryId,
                        budgetIds: i.budgetIds,
                        description: i.description,
                        tagIds: i.tagIds
                    })),
                    deductions: data.deductions.map(i => ({ deductionId: i.deductionId, amount: i.amount})),
                })
                .execute()

            isSuccess = true
        } catch(err) {
            resError = (err as NuxtError).data as ErrorResponse
        }
    }

    if (isSuccess) {
        toast.add({
            title: "Success",
            description: "Facture Ajouter",
            color: 'success'
        })
 
        form.state = "Pending";
        form.transactions = [{
            amount: 0,
            description: '',
            categoryId: '',
            tagIds: [],
            budgetIds: []
        }];
        form.deductions = [];
        if (switchMoreState.value === false) {
            form.accountId = "";
            form.type = "";
            emit('close', true);
        }
    } else {
        toast.add({
            title: resError?.error,
            description: resError?.message,
            color: 'error'
        })
    } 
}

</script>

<template>
    <UModal 
        :title="invoice ? 'Modifier la transaction' : 'Nouvelle transaction'"
        :ui="{ body: 'max-w-4xl' }"
        :dismissible="false"
    >
        <template #body>
            <FormInvoice
                v-if="!isLoading"
                :is-update="invoice != undefined"
                :accounts="utils?.accounts || []"
                :budgets="utils?.budgets || []" 
                :categories="utils?.categories || []"
                :tags="utils?.tags || []"
                :invoice-types="utils?.invoiceTypes.map(i => ({ label: i.value, value: i.id})) || []"
                :deductions="utils?.deductions || []"
                :validate="validate"
                :init-switch-more="switchMoreState"
                v-model="form"
                @submit="onSubmit"
                @close="emit('close', true)"
                @switch-more="state => switchMoreState = state"
            />
            <div v-else class="h-full">
                <LoadingIndicator  /> 
            </div>
        </template> 
    </UModal>
</template>