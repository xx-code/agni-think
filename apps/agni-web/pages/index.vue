<script setup lang="ts">
import { computed } from "vue";
import type { AccountCheckingDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType, EditAccountType } from "~/types/ui/account";
import { getLocalTimeZone } from "@internationalized/date";
import { ModalEditAccount, ModalInvoice, SlideOverQuickInvoicesView } from "#components";
import { createAccount, deleteAccount, fetchAccountsWithDetail, fetchAccountWithDetail, updateAccount } from "~/composables/api/accounts";
import { fetchAccountTypes } from "~/composables/api/internal";

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


const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const modalInvoice = overlay.create(ModalInvoice);
const slideOverQuickInvoices = overlay.create(SlideOverQuickInvoicesView)

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

async function openModalEditInvoice(accountId?: string) {
    const instant = modalInvoice.open({
        invoice: undefined,
        accountSelectedId: accountId,
    });

    await instant.result
    refreshAccounts()
}

const onDeleteAccount = async (accountId: string) => {
    const doDelete = confirm();
    if (doDelete) {
        await deleteAccount(accountId);
        refreshAccounts();
    }
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
        refreshAccounts()
    } catch (err) {
        console.log(err)
    } 
}

const availableBalance = computed(() => {
    return totalAccountBalance.value.totalBalance + Math.abs(totalAccountBalance.value.totalFreezedBalance + totalAccountBalance.value.totalLockedBalance) 
})

function formatAccountBuffer(detail: AccountCheckingDetailType): number {
    return roundNumber(detail.buffer) 
}

function isBufferValid(buffer: number, balance: number): boolean {
    return balance >= buffer 
}

</script>

<template>
    <div class="p-6 space-y-10">
        <!-- Header avec bouton d'ajout -->
        <div class="flex justify-between items-center">
            <h1 class="font-bold text-2xl">Mon portefeuille</h1>
            <UButton 
                icon="i-lucide-plus" 
                size="lg" 
                variant="solid" 
                color="primary"
                @click="openAccountModal()">
                Ajouter un compte
            </UButton>
        </div>

        <UiOverviewAccountSummary 
            :total-balance="totalAccountBalance.totalBalance"
            :disponible="availableBalance"
            :freeze="totalAccountBalance.totalFreezedBalance"
            :lock="totalAccountBalance.totalLockedBalance"
        />
                
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
                            :id="account.id"
                            :title="account.title"
                            :balance="account.balance"
                            :diff-past-balance-per="0"
                            :is-positif="true"
                            :freezed-balance="account.freezedBalance"
                            :locked-balance="account.lockedBalance"
                            allow-edit
                            allow-delete 
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