<script setup lang="ts">
import { computed } from "vue";
import type { AccountCheckingDetailType, AccountCreditDetailType, Account, AccountWithDetailType, EditAccount } from "~/types/ui/account";
import { getLocalTimeZone } from "@internationalized/date";
import { ModalEditAccount, ModalInvoice, SlideOverQuickInvoicesView } from "#components";
import { createAccount, deleteAccount, fetchAccountsWithDetail, fetchAccountWithDetail, updateAccount } from "~/composables/api/accounts";
import { accountWithDetailToAccountCard } from "~/mappers/account";
import { fetchBalanceByPeriod } from "~/composables/api/invoices";
import { getOrderAccountType } from "~/types/constants/account";

const isLoadingAccount = ref(false)
const { data: accountData, refresh: refreshAccounts } = await useAsyncData(
    'accounts+categories+tags+budgets',
    async () => {
        isLoadingAccount.value = true
        const res = await fetchAccountsWithDetail({ offset: 0, limit: 0, queryAll: true })
        const accIds = res.items.map(account => account.id)

        const dateFrom = new Date()
        dateFrom.setMonth(dateFrom.getMonth() - 7)

        const balancesByPeriod = await Promise.all(
            accIds.map(id =>
                fetchBalanceByPeriod({
                    period: 'Month',
                    interval: 1,
                    dateFrom: dateFrom.toISOString(),
                    accountIds: [id]
                })
            )
        )

        isLoadingAccount.value = false

        return {
            accounts: res.items.sort((a, b) => getOrderAccountType(a.type) - getOrderAccountType(b.type)),
            balanceHistories: accIds.map((id, index) => ({
                id,
                histories: balancesByPeriod[index]?.map(i => i.balance) ?? []
            }))
        }
    }
)

const totalAccountBalance = computed(() => {
    let total = 0 
    let totalFreezed = 0
    let totalLocked = 0

    if (accountData.value) {
        for(const acc of accountData.value.accounts) {
            if (acc.type !== 'Saving' && acc.type !== 'Broking')
                total += acc.balance
            totalFreezed += acc.freezedBalance
            totalLocked += acc.lockedBalance
        }
    }  

    return { totalBalance: total, totalFreezedBalance: totalFreezed, totalLockedBalance: totalLocked }
})


const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const modalInvoice = overlay.create(ModalInvoice);
const slideOverQuickInvoices = overlay.create(SlideOverQuickInvoicesView)

const toast = useToast();
const onSaveAccount = async (value: EditAccount, oldValue?: Account) => {
    try {
        if (oldValue)
            await updateAccount(oldValue.id, {
                title: value.title,
                type : value.type,
                color: value.color,
                detail: {
                    contributionType: value.contributionType,
                    managementAccount: value.managementType,
                    creditLimit: value.creditLimit,
                    invoiceDate: value.invoiceDate?.toDate(getLocalTimeZone()).toISOString()
                }
            });
        else 
            await createAccount({
                title: value.title,
                type : value.type,
                color: value.color,
                detail: {
                    contributionType: value.contributionType,
                    managementAccount: value.managementType,
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
    const doDelete = confirm('Voulez vous supprimer cette page');
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
        const instance = slideOverQuickInvoices.open({
            account: account,
            onClose: (refresh) => {
                if (refresh)
                    refreshAccounts()
            } 
        })
        await instance.result
    } catch (err:any) {
        toast.add({
            title: 'Error open transaction view',
            description: err.message ?? 'Error while transactions view try to open',
            color: 'error'
        });
    } 
}

const availableBalance = computed(() => {
    return totalAccountBalance.value.totalBalance + Math.abs(totalAccountBalance.value.totalFreezedBalance + totalAccountBalance.value.totalLockedBalance) 
})

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
            v-if="!isLoadingAccount"
            :total-balance="totalAccountBalance.totalBalance"
            :disponible="availableBalance"
            :freeze="totalAccountBalance.totalFreezedBalance"
            :lock="totalAccountBalance.totalLockedBalance"
        />
        <LoadingIndicator v-else />

        <div class="grid grid-cols-2">
            <div class="col-1">
                <h1 class="text-lg text-gray-500 font-bold">Comptes</h1>
                <div class="grid grid-cols-2 gap-4" v-if="!isLoadingAccount">
                    <UiOverviewCardAccount 
                        v-for="account in accountData?.accounts"
                        :key="account.id"
                        :account="accountWithDetailToAccountCard(account, accountData?.balanceHistories.find(el => el.id === account.id)?.histories ?? [])"
                        @click="() => openTransactionViews(account.id)"
                        @update="() => openAccountModal(account.id)"
                        @delete="() => onDeleteAccount(account.id)"
                    />
                </div> 
                <LoadingIndicator v-else />
            </div>
            
            <div class="col-2">

            </div>
        </div>

    </div> 
</template>

<style scoped lang="scss">
</style>