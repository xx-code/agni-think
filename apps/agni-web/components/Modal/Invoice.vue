<script setup lang="ts">
import { reactive } from "vue";
import type { InvoiceType } from '~/types/ui/transaction';
import type { FormError, FormSubmitEvent } from '#ui/types';
import { fetchAccounts } from '~/composables/api/accounts';
import { fetchBudgets } from '~/composables/api/budget';
import { fetchCategories } from '~/composables/api/categories';
import { fetchDeductions } from '~/composables/api/deductionType';
import { fetchTransactionTypes } from '~/composables/api/internal';
import { fetchTags } from '~/composables/api/tag';
import type { EditInvoiceType } from '~/types/form/invoice';
import { useCreateInvoice, useUpdateInvoice } from '~/composables/api/invoices';

const { invoice, accountSelectedId } = defineProps<{
    invoice?: InvoiceType
    accountSelectedId?: string
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditInvoiceType, oldValue?: InvoiceType): void    
    (e: 'close', close: boolean): void
}>();

const toast = useToast()
const switchMoreState = ref(false)

const { data: utils } = useAsyncData('utils+deduction+edit-invoices', async () => {
    const query = {offset: 0, limit: 0, queryAll: true, isSystem: false}
    const [ categories, tags, budgets, accounts, invoiceTypes, deductions ] = await Promise.all([
        fetchCategories(query),
        fetchTags(query),
        fetchBudgets(query),
        fetchAccounts(query),
        fetchTransactionTypes(),
        fetchDeductions(query)
    ])

    categories.items

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
        categoryId: r.categoryId,
        tagIds: r.tagRefs || [],
        budgetIds: r.budgetRefs || []
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
    let resError = undefined

    if (invoice) {
        const transactionRemovedIds = invoice.transactions.filter(i => !data.transactions.find(
            v => 
                v.amount === i.amount && 
                v.categoryId === i.categoryId &&
                v.budgetIds.length === i.budgetRefs.length &&
                v.budgetIds.every(b => i.budgetRefs.includes(b)) &&
                v.tagIds.length === i.tagRefs.length &&
                v.tagIds.every(t => i.tagRefs.includes(t)) &&
                v.description === i.description
            )).map(i => i.id)

        const transactionAdded = data.transactions.filter(i => !invoice.transactions.find(
            v => 
                v.amount === i.amount && 
                v.categoryId === i.categoryId &&
                v.budgetRefs.length === i.budgetIds.length &&
                v.budgetRefs.every(b => i.budgetIds.includes(b)) &&
                v.tagRefs.length === i.tagIds.length &&
                v.tagRefs.every(t => i.tagIds.includes(t)) &&
                v.description === i.description
            ))

        const res = await useUpdateInvoice(invoice.id, {
            addTransactions: transactionAdded, 
            mouvement: data.mouvement,
            removeTransactionIds: transactionRemovedIds,
            deductions: data.deductions.map(i => ({ deductionId: i.deductionId, amount: i.amount})),
            accountId: data.accountId,
            date: data.date,
            type: data.type
        });

        isSuccess = res.success
        resError = res.error
    } else {
        const res = await useCreateInvoice({
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
        });

        isSuccess = res.success
        resError = res.error
    }

    if (isSuccess) {
        form.accountId = "";
        form.state = "Pending";
        form.type = "";
        form.transactions = [{
            amount: 0,
            description: '',
            categoryId: '',
            tagIds: [],
            budgetIds: []
        }];
        form.deductions = [];

        toast.add({
            title: "Success",
            description: "Facture Ajouter",
            color: 'success'
        })

        if (switchMoreState.value === false)
            emit('close', true);
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
        </template> 
    </UModal>
</template>