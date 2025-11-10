<script setup lang="ts">
import { computed, ref } from "vue";
import { ALL_ACCOUNT_ID } from "~/composables/accounts/useAccountsWithPastBalance";
import useDeleteAccount from "~/composables/accounts/useDeleteAccount";
import type { AccountBrokeDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType, EditAccountType } from "~/types/ui/account";
import useCreateAccount from "~/composables/accounts/useCreateAccount";
import useUpdateAccount from "~/composables/accounts/useUpdateAccount";
import useTransactionPagination from "~/composables/transactions/useTransactionPagination";
import { ModalEditAccount, ModalEditFreezeTransaction, ModalEditTransaction, ModalEditTransfer } from "#components";
import { fetchAccount, fetchAccountWithDetail } from "~/composables/accounts/useAccount";
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
import { fetchAccountsWithDetail } from "~/composables/accounts/useAccounts";
import { fetchAccountTypes } from "~/composables/internals/useAccountTypes";

type AccountByType = {
    id: string
    title: string
    accounts: AccountWithDetailType[]
}

const { data: accounts, refresh: refreshAccounts } = await useAsyncData('accounts+categories+tags+budgets', async () => {
    const res = await fetchAccountsWithDetail({ offset: 0, limit: 0, queryAll: true })
    const accountTypes = await fetchAccountTypes()

    const accountsByType = []
    for(const type of accountTypes) {

        const accounts = res.items.filter(i => i.type === type.id)
        accountsByType.push({
            id: type.id,
            title: type.value,
            accounts: accounts
        } satisfies AccountByType) 
    }

    return accountsByType
})

const totalAccountBalance = computed(() => {
    let total = 0 
    let totalFreezed = 0
    let totalLocked = 0

    if (accounts.value) {
        for(const acc of accounts.value) {
            if (acc.id !== 'Saving' && acc.id !== 'Broking')
                total += acc.accounts.reduce((acc, curr) => acc += curr.balance, 0)
            totalFreezed += acc.accounts.reduce((acc, curr) => acc += curr.freezedBalance, 0)
            totalLocked += acc.accounts.reduce((acc, curr) => acc += curr.lockedBalance, 0)
        }
    }  

    return { totalBalance: total, totalFreezedBalance: totalFreezed, totalLockedBalance: totalLocked }
})

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
    if (id !== ALL_ACCOUNT_ID) {
        paramsTransaction.accountFilterIds = [id]
    }

    onUpateAccount(id)
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
    let account: AccountWithDetailType |undefined;
    if (accountId) {
        account = await fetchAccountWithDetail(accountId);
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

async function openModalEditTransaction(accountId?: string) {
    // let transaction:TransactionType|undefined;
    // if (transactionId)
    //     transaction = await fetchTransaction(transactionId);

    const instant = modalTransaction.open({
        transaction: undefined,
        accountSelectedId: accountId,
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

</script>

<template>
    <div  style="margin-top: 1rem;">
        <div class="self-end" style="margin-bottom: 1rem;">
            <UButton icon="i-lucide-plus" size="xl" variant="solid" @click="openAccountModal()">Ajouter Carte</UButton>
        </div>

        <div class="grid md:grid-cols-2 gap-2">
            <div class="bg-white rounded-md p-2 border-1 border-gray-200">
                <div class="text-xs text-gray-500">Total Balance</div> 
                <div class="flex items-start justify-between">
                    <div class="card-money" style="margin-top: 1rem;">
                        <AmountTitle 
                            :amount="totalAccountBalance.totalBalance"
                            :sign="'$'"
                        />
                        <div class="text-xs text-gray-300">
                            <p>Freezed Balance: ${{ totalAccountBalance.totalFreezedBalance.toFixed(2) ?? 0 }}</p>
                            <p>Locked Balance: ${{ totalAccountBalance.totalLockedBalance.toFixed(2) ?? 0 }}</p>
                        </div>
                    </div>
                    <div className="text-right">
                        <div className="mt-3 flex gap-2">
                            <UButton icon="i-lucide-banknote" size="md" @click="openModalEditTransaction()"/>
                            <UButton icon="i-lucide-arrow-right-left" size="md" @click="openModalTransferAccount(selectedAccountId)" />
                            <UButton icon="i-lucide-snowflake" size="md" @click="openModalEditFreezeTransaction(selectedAccountId)"/>
                        </div>
                    </div>
                </div> 
                
                <div className="mt-4">
                    <div className="w-full bg-gray-100 rounded-full h-3 overflow-hidden">
                        <div className="h-3 rounded-full" style="width:50%; background:linear-gradient(90deg,#10b981,#60a5fa)" />
                    </div>
                    <div className="flex justify-between text-xs text-gray-400 mt-2">
                        <span>Disponible</span>
                        <span>Verrouillé</span>
                    </div>
                </div>
            </div>


            <div class="bg-white rounded-md p-2 border-1 border-gray-200">
                <h2 class="text-xs text-gray-500">Gestion financière responsable</h2> 
                <div className="space-y-2 text-sm text-gray-600" >
                    <div className="p-3 rounded-md bg-emerald-50 border">📈 Dépenses mensuelles: <strong>--%</strong></div>

                    <div className="mt-3">
                        <button className="w-full px-4 py-2 bg-indigo-600 text-white rounded-md">Voir recommandations</button>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="space-y-2 mt-2">
            <div v-for="group in accounts">
                <h3 
                    class="font-black"
                    v-if="group.accounts.length > 0">
                    {{ group.title }}
                </h3>
                <div class="xs:col-1 sm:col-span-1 md:col-span-3 flex flex-col" >
                    <!-- <div class="self-end" style="margin-bottom: 1rem;">
                        <UButton icon="i-lucide-plus" size="xl" variant="solid" @click="openAccountModal()">Ajouter Carte</UButton>
                    </div> -->
                    <div class="flex overflow-x-auto gap-2"  >
                        <div v-for="account in group.accounts" :key="account.id">
                            <CardResumeAccount 
                                @customClick="onSelectAccount(account.id)"
                                style="width: 200px;"
                                v-if="account.id !== selectedAccountId"
                                :id="account.id"
                                :title="account.title"
                                :balance="account.balance"
                                :diff-past-balance-per="0"
                                :is-positif="true"
                                :freezed-balance="account.freezedBalance"
                                :locked-balance="account.lockedBalance"
                                :allow-edit="account.id === ALL_ACCOUNT_ID ? false : true"
                                :allow-delete="account.id === ALL_ACCOUNT_ID ? false :true" 
                                @add="openModalEditTransaction(account.id)"
                                @edit="openAccountModal(account.id)"
                                @delete="onDeleteAccount(account.id)"> 

                                <div v-if="account.type === 'CreditCard'">
                                    <p class="text-sm text-gray-500">Limit de credit: {{ (account.detail as AccountCreditDetailType).creditLimit }}</p>
                                    <p class="text-sm font-semibold" :style="{color: creditUilisationToColor((account.detail as AccountCreditDetailType).creditUtilisation)}">
                                        Utilisation: {{ (account.detail as AccountCreditDetailType).creditUtilisation }} %
                                    </p>
                                </div>
                            </CardResumeAccount>
                        </div>
                    </div> 
                </div>
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