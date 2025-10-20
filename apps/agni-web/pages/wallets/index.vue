<script setup lang="ts">
import { computed, ref } from "vue";
import useAccountsWitPastBalance, { ALL_ACCOUNT_ID } from "~/composables/accounts/useAccountsWithPastBalance";
import useDeleteAccount from "~/composables/accounts/useDeleteAccount";
import type { AccountType, EditAccountType } from "~/types/ui/account";
import useCreateAccount from "~/composables/accounts/useCreateAccount";
import useUpdateAccount from "~/composables/accounts/useUpdateAccount";
import useTransactionPagination from "~/composables/transactions/useTransactionPagination";
import { ModalEditAccount, ModalEditFreezeTransaction, ModalEditTransaction, ModalEditTransfer } from "#components";
import { fetchAccount } from "~/composables/accounts/useAccount";
import type { EditFreezeTransactionType, EditTransactionType, EditTransfertType, TransactionTableType, TransactionType } from "~/types/ui/transaction";
import useUpdateTransaction from "~/composables/transactions/useUpdateTransaction";
import useCreateTransaction from "~/composables/transactions/useCreateTransaction";
import { fetchTransaction } from "~/composables/transactions/useTransaction";
import useFreezeTransaction from "~/composables/transactions/useFreezeTransaction";
import useTransfertTransaction from "~/composables/transactions/useTransfertTransaction";
import type { FilterTransactionQuery } from "~/types/api/transaction";
import useCategories from "~/composables/categories/useCategories";
import useTags from "~/composables/tags/useTags";
import { getLocalTimeZone } from "@internationalized/date";
import useBudgets from "~/composables/budgets/useBudgets";
import useAnalyseBudgetRules from "~/composables/analytics/useBudgetRules";


const {data: accounts, error: errorAccounts, refresh: refreshAccounts} = useAccountsWitPastBalance({ 
    period: 'Month', periodTime: 1, offset: 0, limit: 0, queryAll: true
}); 
const {data: categories, error: errorCategories, refresh: refreshCategories} = useCategories({
    limit: 0, offset: 0, queryAll: true
});
const {data: tags, error: errorTags, refresh: refreshTag} = useTags({
    limit: 0, offset: 0, queryAll: true
});
const {data: budgets, error: errorBudgets, refresh: refreshBudget } = useBudgets({
    limit: 0, offset: 0, queryAll: true
});

const selectedAccountId = ref(ALL_ACCOUNT_ID);

const getCategory = (id: string) => categories.value?.items.find(i => i.id === id);
const getTag = (id: string) => tags.value?.items.find(i => i.id === id);
const getBudget = (id: string) => budgets.value?.items.find(i => i.id === id);

const pageTransaction = ref(1)
const pageFreezeTransaction = ref(1)
const paramsTransaction = reactive<FilterTransactionQuery>({offset: 0, limit: 4, isFreeze: false});
const {data: transactions, error: errorTransactions, refresh: refreshTransactions} = useTransactionPagination(paramsTransaction) // add compute for change in selected Account
const displayTransactions = computed(() => {

    return transactions.value?.items.map<TransactionTableType>(i => ({
        id:i.id,
        accountId: i.accountId,
        amount: i.amount,
        budgets: i.budgetIds.map(i => ({
            id: i,
            title: getBudget(i)?.title || ''
        })),
        category: {
            id: i.categoryId,
            title: getCategory(i.categoryId)?.title || '',
            color: getCategory(i.categoryId)?.color || '',
            icon: getCategory(i.categoryId)?.icon || ''
        },
        date: i.date,
        description: i.description,
        recordType: i.recordType,
        status: i.status,
        tags: i.tagIds.map(i => ({ 
            id: i,
            value: getTag(i)?.value || '',
            color: getTag(i)?.color || ''
        })),
        type: i.type
    }))
});

const paramsFreezeTransaction = reactive<FilterTransactionQuery>({offset: 0, limit: 4, isFreeze: true});
const { data:freezeTransactions, refresh:refreshFreezeTransactions } = useTransactionPagination(paramsFreezeTransaction);
const displayFutureTransactions = computed(() => {
    const allFutureTransactions: TransactionTableType[] = []; 

    freezeTransactions.value?.items.forEach(i => {
        allFutureTransactions.push({
            id:i.id,
            accountId: i.accountId,
            amount: i.amount,
            budgets: [],
            category: {
                id: i.categoryId,
                title: getCategory(i.categoryId)?.title || '',
                color: getCategory(i.categoryId)?.color || '',
                icon: getCategory(i.categoryId)?.icon || ''
            },
            date: i.date,
            description: i.description,
            recordType: i.recordType,
            status: i.status,
            tags: i.tagIds.map(i => ({ 
                id: i,
                value: getTag(i)?.value || '',
                color: getTag(i)?.color || ''
            })),
            type: i.type
        });
    });

    return allFutureTransactions;
})

const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const modalTransfer = overlay.create(ModalEditTransfer);
const modalTransaction = overlay.create(ModalEditTransaction);
const modalFreezeTransaction = overlay.create(ModalEditFreezeTransaction);

const onSelectAccount = (id: string) => {
    selectedAccountId.value = id
    onUpateAccount(id)
}
const getAccount = (id: string) => {
    if (accounts.value)
        return accounts.value.items.find(acc => acc.id === id)
    return null
}

const toast = useToast();
const onSaveAccount = async (value: EditAccountType, oldValue?: AccountType) => {
    try {
        if (oldValue)
            await useUpdateAccount(oldValue.id, value);
        else 
            await useCreateAccount(value);
        
        refreshAccounts();
    } catch(err) {
        toast.add({
            title: 'Error',
            description: `Error while ${oldValue ? 'Update' : 'Create'} account`,
            color: 'error'
        });
    }
}

const openAccountModal = async (accountId?: string) => {
    let account:AccountType|undefined;
    if (accountId) {
        account = await fetchAccount(accountId);
    }
        
    modalAccount.open({
        account: account,
        onSubmit: onSaveAccount 
    }); 
}

async function onTransfertAccount(value: EditTransfertType) {
    try {
        await useTransfertTransaction({
            accountIdFrom: value.accountIdFrom,
            accountIdTo: value.accountIdTo,
            amount: value.amount,
            date: value.date.toString()
        })
        refreshTransactions()
        refreshAccounts()
    } catch(err) {
        toast.add({
            title: 'Error tranfert',
            description: 'Error while transfert account',
            color: 'error'
        });
    }
} 

async function openModalTransferAccount (accountId?: string){ 
    const filterId = accountId === ALL_ACCOUNT_ID ? undefined : accountId
        
    const instance = modalTransfer.open({
        accountId: filterId,
        onSubmit: onTransfertAccount 
    });

    const shouldRefresh = await instance.result; 
}

async function onSubmitTransaction(value: EditTransactionType, oldValue?: TransactionType) {
    try {
        if (oldValue)
            await useUpdateTransaction(oldValue.id, {
                accountId: value.accountId,
                amount: value.amount,
                budgetIds: value.budgetIds,
                categoryId: value.categoryId,
                date: value.date.toString(),
                description: value.description,
                tagIds: value.tagIds,
                type: value.type
            });
        else 
            await useCreateTransaction({
                accountId: value.accountId,
                amount: value.amount,
                budgetIds: value.budgetIds,
                categoryId: value.categoryId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                description: value.description,
                tagIds: value.tagIds,
                type: value.type
            });
        refreshTransactions()
        refreshAccounts()
    } catch(err) {
        toast.add({
            title: 'Error submit transaction',
            description: 'Error while submit transaction ' + err,
            color: 'error'
        })
    }
}

async function openModalEditTransaction(transactionId?: string) {
    let transaction:TransactionType|undefined;
    if (transactionId)
        transaction = await fetchTransaction(transactionId);

    const instant = modalTransaction.open({
        transaction: transaction,
        accountSelectedId: selectedAccountId.value,
        onSubmit: onSubmitTransaction 
    });

    const shouldRefresh = await instant.result
}

async function onFreezeTransaction(value: EditFreezeTransactionType) {
    try {
        await useFreezeTransaction({
            accountId: value.accountId,
            amount: value.amount,
            endDate: value.endDate.toString()
        })
        refreshFreezeTransactions();
        refreshAccounts()
    } catch(err) {
        toast.add({
            title: 'Error Freeze',
            description: 'Error while freeze account',
            color: 'error'
        });
    }
}

async function openModalEditFreezeTransaction(accountId: string = '') {
    const instance = modalFreezeTransaction.open({
        onSubmit: onFreezeTransaction
    });

    await instance.result; 

}

const onDeleteAccount = async (accountId: string) => {
    const doDelete = confirm();
    if (doDelete) {
        await useDeleteAccount(accountId);
        refreshAccounts();
        refreshTransactions()
    }
}

const onUpateAccount = async (payload: string) => {
    let filterAcc: string[] = []
    if (payload !== ALL_ACCOUNT_ID)
        filterAcc = [payload] 
}

// const { data: analyseBudgetRules } = useAnalyseBudgetRules({ period: 'Month', periodTime: 1 })

// const optionsChart = computed(() => ({
//     responsive: true,
//     plugins: {colors: { forceOverride: true}}
// })) 
// const dataChart = computed(() => {
//     let labels: string[] = []
//     let data: number[] = []
//     if (analyseBudgetRules.value) {
//         labels = analyseBudgetRules.value.map(i => i.transactionType)
//         data = analyseBudgetRules.value.map( i => i.value)    
//      } 

//     return {
//         labels: labels,
//         datasets: [{
//             label: '50/20/30 rules match',
//             data: data
//         }]
//     } 
// })

</script>

<template>
    <div class="grid xs:grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-1" style="margin-top: 1rem;">
        <div>
            <div class="card rounded-md">
                <CustomCardTitle :title="getAccount(selectedAccountId)?.title">
                   <USelect 
                    v-model="selectedAccountId" 
                    @update:modelValue="onUpateAccount" 
                    value-key="id"  label-key="title" 
                    :items="accounts?.items.map(acc => ({ 
                        id: acc.id, 
                        title: acc.title, 
                        type: 'item' }))"
                    /> 
                </CustomCardTitle>
                <div class="card-money" style="margin-top: 1rem;">
                    <h2>
                        ${{ getAccount(selectedAccountId)?.balance }}
                    </h2>
                </div>

                <div class="card-bottom" style="margin-top: 1rem;">
                    <div class="flex items-center">
                        <UBadge 
                            variant="subtle"
                            :color="getAccount(selectedAccountId)?.pastBalanceDetail.doIncrease ? 'success' : 'error'"
                            :icon="getAccount(selectedAccountId)?.pastBalanceDetail.doIncrease ? 'i-lucide-arrow-up' : 'i-lucide-arrow-down'">
                            {{ getAccount(selectedAccountId)?.pastBalanceDetail.diffPercent }}%
                        </UBadge>
                        <p>Vs {{ }} precendent</p>
                    </div>
                </div>

                <div class="flex justify-between mt-5">
                    <UButton icon="i-lucide-banknote" size="xl" @click="openModalEditTransaction()"/>
                    <UButton icon="i-lucide-arrow-right-left" size="xl" @click="openModalTransferAccount(selectedAccountId)" />
                    <UButton icon="i-lucide-snowflake" size="xl" @click="openModalEditFreezeTransaction(selectedAccountId)"/>
                </div>
            </div>
        </div>

        <div class="xs:col-1 sm:col-span-1 md:col-span-3 flex flex-col" >
            <div class="self-end" style="margin-bottom: 1rem;">
                <UButton icon="i-lucide-plus" size="xl" variant="solid" @click="openAccountModal()">Ajouter Carte</UButton>
            </div>
            <div class="flex overflow-x-auto gap-2"  >
                <div v-for="account in accounts?.items" :key="account.id">
                    <CardResumeAccount 
                        @customClick="onSelectAccount(account.id)"
                        style="width: 200px;"
                        v-if="account.id !== selectedAccountId"
                        :id="account.id"
                        :title="account.title"
                        :balance="account.balance"
                        :diff-past-balance-per="account.pastBalanceDetail.diffPercent"
                        :is-positif="account.pastBalanceDetail.doIncrease"
                        :allow-edit="account.id === ALL_ACCOUNT_ID ? false : true"
                        :allow-delete="account.id === ALL_ACCOUNT_ID ? false :true" 
                        @edit="openAccountModal(account.id)"
                        @delete="onDeleteAccount(account.id)"

                    /> 
                </div>
            </div> 
        </div>
    </div> 

    <div class="grid md:grid-cols-3 gap-2" style="margin-top: 1rem;">
        <div class="card rounded-md col-span-2 ">
            <CustomCardTitle title="Recente transaction">
                <div class="flex gap-1">
                    <!-- <USelect class="rounded-full" v-model="transactionAccountSelected" :items="accounts.map(acc => (acc.title))" /> -->
                    <UButton class="rounded-full" size="sm" label="Voir plus" variant="outline" color="neutral" />
                </div>
            </CustomCardTitle>
            <div class="flex flex-col gap-1" style="margin-top: 1rem;">
                <div v-for="trans in displayTransactions" :key="trans.id">
                    <RowTransaction 
                        :id="trans.id" 
                        :balance="trans.amount" 
                        :title="trans.category.title" 
                        :description="trans.description" 
                        :icon="trans.category.icon" 
                        :color="trans.category.color"
                        :tags="trans.tags.map((tag: any)=>tag.value)" 
                        :date="trans.date" 
                        :recordType="trans.recordType"
                        :doShowEdit="true"
                    />
                </div>
                <UPagination 
                    class="mt-3" 
                    v-model:page="pageTransaction" 
                    v-on:update:page="() => paramsTransaction.offset = paramsTransaction.limit * (pageTransaction -1)"
                    :items-per-page="paramsTransaction.limit"  
                    :total="Number(transactions?.totals)" 
                    active-variant="subtle" />
            </div>
        </div>
    
        <div class="card rounded-md md:row-span-2">
            <CustomCardTitle title="Gestion financière responsable">

            </CustomCardTitle>
            <div>
                <!-- <DoughnutChart :data="dataChart" :options="optionsChart"/> -->
            </div>
        </div>

        <div class="card rounded-md col-span-2 ">
            <CustomCardTitle title="Freeze transactions">
            </CustomCardTitle>
            <div class="flex flex-col gap-1" style="margin-top: 1rem;">
                <div v-for="trans in displayFutureTransactions" :key="trans.id">
                    <RowTransaction 
                        :id="trans.id" 
                        :balance="trans.amount" 
                        :title="trans.category.title" 
                        :description="trans.description" 
                        :icon="trans.category.icon" 
                        :tags="trans.tags.map((tag: any) => tag.value)" 
                        :date="trans.date" 
                        :color="trans.category.color"
                        :recordType="trans.recordType"
                        :doShowEdit="false"
                    />    
                </div>
                <UPagination 
                    class="mt-3" 
                    v-model:page="pageFreezeTransaction" 
                    v-on:update:page="(p) => paramsFreezeTransaction.offset = paramsFreezeTransaction.limit * (p -1)"
                    :items-per-page="paramsFreezeTransaction.limit"  
                    :total="Number(freezeTransactions?.totals)" 
                    active-variant="subtle" />
            </div>
        </div>

        
    </div>
</template>

<style scoped lang="scss">
.card {
    border: solid 1px #E6E6E6;
    padding: 0.5rem;
    background: rgb(250, 251, 255);

    .card-money {
        font-weight: bold;
        font-size: x-large;
        color: #1E3050;
    }
    
    .card-bottom {
        p {
            font-size: small;
            margin-left: 2.5px;
            color: #ADADAD;
        }
    }
    
}
</style>