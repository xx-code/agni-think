<script setup lang="ts">
import { computed, ref } from "vue";
import type { AccountCheckingDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType, EditAccountType } from "~/types/ui/account";
import type { EditFreezeInvoiceType, EditTransfertType, InvoiceType } from "~/types/ui/transaction";
import { getLocalTimeZone } from "@internationalized/date";
import type { QueryFilterRequest } from "~/types/api";
import type { QueryInvoice } from "~/types/api/transaction";
import { ModalEditAccount, ModalEditFreezeInvoice, ModalEditTransfer, ModalInvoice, SlideOverQuickInvoicesView } from "#components";
import { createAccount, deleteAccount, fetchAccountsWithDetail, fetchAccountWithDetail, updateAccount } from "~/composables/api/accounts";
import { fetchAccountTypes } from "~/composables/api/internal";
import { useFreezeInvoice, useTransfertInvoice } from "~/composables/api/invoices";

const ALL_ACCOUNT_ID = "ALL_ACCOUNT_ID"

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

        const accounts = res.items.filter(i => i.type.toLowerCase() === type.id.toLowerCase())
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

const selectedAccountId = ref(ALL_ACCOUNT_ID);

const paramsTransaction = reactive<QueryFilterRequest & QueryInvoice>({offset: 0, limit: 4, isFreeze: false});

const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const modalTransfer = overlay.create(ModalEditTransfer);
const modalInvoice = overlay.create(ModalInvoice);
const modalFreezeInvoice = overlay.create(ModalEditFreezeInvoice);
const slideOverQuickInvoices = overlay.create(SlideOverQuickInvoicesView)

const onSelectAccount = (id: string) => {
    selectedAccountId.value = id
    if (id !== ALL_ACCOUNT_ID) {
        paramsTransaction.accountIds = [id]
    }

    onUpateAccount(id)
}

const toast = useToast();
const onSaveAccount = async (value: EditAccountType, oldValue?: AccountType) => {
    try {
        if (oldValue)
            await updateAccount(oldValue.id, {
                title: value.title,
                type : value.type,
                detail: {
                    contributionType: value.contributionType,
                    managementAccountType: value.managementType,
                    creditLimit: value.creditLimit,
                    invoiceDate: value.invoiceDate?.toDate(getLocalTimeZone()).toISOString()
                }
            });
        else 
            await createAccount({
                title: value.title,
                type : value.type,
                detail: {
                    contributionType: value.contributionType,
                    managementAccountType: value.managementType,
                    creditLimit: value.creditLimit,
                    invoiceDate: value.invoiceDate?.toDate(getLocalTimeZone()).toISOString()
                }
            });
        
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
        await useTransfertInvoice({
            accountIdFrom: value.accountIdFrom,
            accountIdTo: value.accountIdTo,
            amount: value.amount,
            date: value.date.toDate(getLocalTimeZone()).toISOString()
        })
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

async function openModalEditInvoice(accountId?: string) {
    const instant = modalInvoice.open({
        invoice: undefined,
        accountSelectedId: accountId,
    });

    await instant.result
    refreshAccounts()
}

async function onFreezeInvoice(value: EditFreezeInvoiceType) {
    try {
        await useFreezeInvoice({
            accountId: value.accountId,
            title: value.title,
            amount: value.amount,
            endDate: value.endDate.toDate(getLocalTimeZone()).toISOString(),
            status: value.status
        })
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
    const instance = modalFreezeInvoice.open({
        onSubmit: onFreezeInvoice
    });

    await instance.result; 

}

const onDeleteAccount = async (accountId: string) => {
    const doDelete = confirm();
    if (doDelete) {
        await deleteAccount(accountId);
        refreshAccounts();
    }
}

const onUpateAccount = async (payload: string) => {
    let filterAcc: string[] = []
    if (payload !== ALL_ACCOUNT_ID)
        filterAcc = [payload] 
}

const computeAllUtilization = (accounts: AccountWithDetailType[]) => {
    const creditCardAccount = accounts.filter(i => i.type === 'CreditCard')
    const sum = creditCardAccount.reduce((acc, account) => acc += (account.detail as AccountCreditDetailType).creditUtilisation, 0)

    return (sum/creditCardAccount.length).toFixed(2)
}

const openTransactionViews = async (accountId: string) => {
    try {
        let account = await fetchAccountWithDetail(accountId);
        slideOverQuickInvoices.open({
            account: account
        })
    } catch (err) {
        console.log(err)
    } 
}

const availableBalance = computed(() => {
    return totalAccountBalance.value.totalBalance + Math.abs(totalAccountBalance.value.totalFreezedBalance + totalAccountBalance.value.totalLockedBalance) 
})

const lockedPercentage = computed(() => {
    if (totalAccountBalance.value.totalBalance <= 0) return 0
    return ((totalAccountBalance.value.totalFreezedBalance + totalAccountBalance.value.totalLockedBalance) / totalAccountBalance.value.totalBalance) * 100
})

function formatAccountBuffer(detail: AccountCheckingDetailType): number {
    return roundNumber(detail.buffer) 
}

function isBufferValid(buffer: number, balance: number): boolean {
    return balance >= buffer 
}

</script>

<template>
    <div class="p-6">
        <!-- Header avec bouton d'ajout -->
        <div class="flex justify-between items-center mb-5">
            <h1 class="font-bold text-2xl">Mon Portefeuille</h1>
            <UButton 
                icon="i-lucide-plus" 
                size="lg" 
                variant="solid" 
                color="primary"
                @click="openAccountModal()">
                Ajouter un compte
            </UButton>
        </div>

        <!-- Cartes principales -->
        <div class="main-cards-grid">
            <!-- Carte Balance Totale -->
            <UCard >
                <template #header>
                    <div class="flex justify-between items-center">
                        <div>
                            <h3 class="text-xl font-bold">Balance Totale</h3>
                            <p>Vue d'ensemble de vos finances</p>
                        </div>
                        <div class="icon-wrapper balance-icon">
                            <Icon name="i-lucide-wallet" class="text-2xl" />
                        </div>
                    </div>
                </template>
                
                <div class="balance-content" @click="">
                    <AmountTitle 
                        :amount="roundNumber(totalAccountBalance.totalBalance)"
                        :sign="'$'"
                        class="main-amount"
                    />
                    
                    <div class="balance-details">
                        <div class="align-baseline text-green-400">
                            <Icon name="i-lucide-circle-check" class="balance-item-icon" />
                            <span class="balance-item-label">Disponible:</span>
                            <span class="balance-item-value">${{ roundNumber(availableBalance, 2) }}</span>
                        </div>
                        <div class="balance-item freezed text-blue-300">
                            <Icon name="i-lucide-snowflake" class="balance-item-icon" />
                            <span class="balance-item-label">Gelé:</span>
                            <span class="balance-item-value">${{ roundNumber(totalAccountBalance .totalFreezedBalance, 2) }}</span>
                        </div>
                        <div class="balance-item locked">
                            <Icon name="i-lucide-lock" class="balance-item-icon" />
                            <span class="balance-item-label">Verrouillé:</span>
                            <span class="balance-item-value">${{ roundNumber(totalAccountBalance.totalLockedBalance, 2) }}</span>
                        </div>
                    </div>
                </div>

                <!-- Actions rapides -->
                <div class="flex gap-2">
                    <UButton 
                        icon="i-lucide-banknote" 
                        size="sm" 
                        variant="soft"
                        color="success"
                        @click="openModalEditInvoice()">
                        Transaction
                    </UButton>
                    <UButton 
                        icon="i-lucide-arrow-right-left" 
                        size="sm" 
                        variant="soft"
                        color="info"
                        @click="openModalTransferAccount(selectedAccountId)">
                        Transfert
                    </UButton>
                    <UButton 
                        icon="i-lucide-snowflake" 
                        size="sm" 
                        variant="soft"
                        color="neutral"
                        @click="openModalEditFreezeTransaction(selectedAccountId)">
                        Geler
                    </UButton>
                </div>

                <!-- Barre de progression -->
                <div class="progress-section">
                    <div class="progress-bar-wrapper">
                        <div class="progress-bar-bg">
                            <div 
                                class="progress-bar-fill" 
                                :style="{ width: `${lockedPercentage}%` }" 
                            />
                        </div>
                    </div>
                    <div class="progress-labels">
                        <span class="progress-label">
                            <span class="progress-dot available"></span>
                            Disponible ({{ roundNumber(100 - lockedPercentage) }}%)
                        </span>
                        <span class="progress-label">
                            <span class="progress-dot locked"></span>
                            Bloqué ({{ roundNumber(lockedPercentage) }}%)
                        </span>
                    </div>
                </div>
            </UCard>

        </div>
        
        <!-- Liste des comptes par type -->
        <div class="mt-4">
            <div v-for="group in accounts" :key="group.id" class="account-group">
                <div class="mt-2" v-if="group.accounts.length > 0">
                    <h2 class="account-group-title">{{ group.title }}</h2>
                    <div v-if="group.id === 'CreditCard'" class="credit-utilization">
                        <Icon name="i-lucide-credit-card" class="mr-1" />
                        <span>Utilisation moyenne:</span>
                        <strong :style="{color: creditUilisationToColor(Number(computeAllUtilization(group.accounts))) }">
                            {{ computeAllUtilization(group.accounts) }}%
                        </strong>
                    </div>
                </div>
                
                <div class="flex gap-3 flex-wrap">
                    <div v-for="account in group.accounts" :key="account.id" class="account-card-wrapper">
                        <CardResumeAccount 
                            @open="openTransactionViews(account.id)"
                            v-if="account.id !== selectedAccountId"
                            :id="account.id"
                            :title="account.title"
                            :balance="account.balance"
                            :diff-past-balance-per="0"
                            :is-positif="true"
                            :freezed-balance="account.freezedBalance"
                            :locked-balance="account.lockedBalance"
                            :allow-edit="account.id !== ALL_ACCOUNT_ID"
                            :allow-delete="account.id !== ALL_ACCOUNT_ID" 
                            @add="openModalEditInvoice(account.id)"
                            @edit="openAccountModal(account.id)"
                            @delete="onDeleteAccount(account.id)"> 

                            <div v-if="account.type === 'CreditCard'" class="credit-card-details">
                                <p class="text-sm text-gray-400">
                                    Prochaine facture: {{ formatDate((account.detail as AccountCreditDetailType).nextInvoicePaymentDate) }}
                                </p>
                                <p class="credit-limit">
                                    <Icon name="i-lucide-landmark" class="inline mr-1" />
                                    Limite: ${{ (account.detail as AccountCreditDetailType).creditLimit }}
                                </p>
                                <p 
                                    class="credit-usage" 
                                    :style="{color: creditUilisationToColor((account.detail as AccountCreditDetailType).creditUtilisation)}">
                                    <Icon name="i-lucide-percent" class="inline mr-1" />
                                    Utilisation: {{ (account.detail as AccountCreditDetailType).creditUtilisation }}%
                                </p>
                            </div>
                            <div v-else-if="account.type == 'Checking'" class="credit-card-details text-sm">
                                <div class="flex items-center justify-between">
                                    <div class="flex items-center">
                                        <Icon name="i-lucide-circle-check" class="text-green-500" />
                                        <span class="ml-1">Disponible:</span>
                                    </div>

                                    <span>${{ roundNumber(account.balance + Math.abs(account.freezedBalance) , 2)  }}</span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <div class="flex items-center">
                                        <Icon 
                                            name="i-lucide-align-horizontal-space-around" 
                                            :class="[isBufferValid(formatAccountBuffer(account.detail as AccountCheckingDetailType), account.balance + account.freezedBalance) ? 'text-green-500' : 'text-red-500']" />
                                        <span class="ml-1">Buffer:</span>
                                    </div>

                                    <span>${{ formatAccountBuffer(account.detail as AccountCheckingDetailType)  }}</span>
                                </div>
                            </div>
                        </CardResumeAccount>
                    </div>
                </div> 
            </div>
        </div> 
    </div> 
</template>

<style scoped lang="scss">
</style>