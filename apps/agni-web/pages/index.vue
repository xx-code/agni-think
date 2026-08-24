<script setup lang="ts">
import { computed } from "vue";
import type { Account, AccountWithDetailType, EditAccount } from "~/types/ui/account";
import { getLocalTimeZone } from "@internationalized/date";
import { ModalEditAccount, SlideOverQuickInvoicesView } from "#components";
import { createAccount, deleteAccount, fetchAccountsWithDetail, fetchAccountWithDetail, updateAccount } from "~/composables/api/accounts";
import {accountWithDetailToAccountCard, listAccountsResponseToListAccountWithDetail } from "~/mappers/account";
import { fetchBalance, fetchBalanceByPeriod } from "~/composables/api/invoices";
import { getOrderAccountType } from "~/types/constants/account";
import { fetchAnalyticSavings, fetchSpendByCategoriesAnalytic } from "~/composables/api/analytics";
import { fetchAllGoal } from "~/composables/api/goals";
import { goalToFundGoalCards } from "~/mappers/goal";
import type { FundCardGoal } from "~/types/ui/fund";
import { API_ROUTES } from "~/shared/routes";

const isLoadingAccount = ref(false)
const isKpiLoading = ref(false)
const isLoadingTopSpend = ref(false)
const isLoadingGoal = ref(false)

const { data: accountData, refresh: refreshAccounts } = useAsyncData(
    'accounts+categories+tags+budgets',
    async () => {
        isLoadingAccount.value = true
        const res = await ApiLinkBuilder
                        .route(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
                        .mapper(listAccountsResponseToListAccountWithDetail)
                        .query({offest: 0, limit: 0, queryAll: true, withDetail: true})
                        .execute()

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

const { data: kpi } = useAsyncData('cashflow+savingrates', async () => {
    isKpiLoading.value = true

    const date = new Date()
    date.setDate(1)

    const [currentBalance, savingBalance] = await Promise.all([        
        fetchBalance({
            startDate: date.toISOString(),
            isFreeze: false
        }),
        fetchAnalyticSavings({
            period: 'Month',
            interval: 1,
            startDate: date.toISOString(),
        })
    ])

    isKpiLoading.value = false

    return {
        cashflow: currentBalance.income - currentBalance.spend,
        savingRate: savingBalance.savingRates[0] ?? 0.0 
    }
}, { watch: [ accountData ]})

const { data: topSpendByCategories } = useAsyncData('top-spend-categories', async () => {
    isLoadingTopSpend.value = true

    const date = new Date()
    date.setDate(1)
    
    const res = await fetchSpendByCategoriesAnalytic({
        period: 'Month',
        interval: 1,
        startDate: date.toISOString(),
        offset: 0,
        limit: 0,
        queryAll: true
    })

    isLoadingTopSpend.value = false

    return res.items
            .map(item => ({ 
                ...item,
                spend: item.spends.at(-1) ?? 0,
            }))
            .filter(i => i.spend > 0).sort((a, b) => b.spend - a.spend).slice(0, 4)

}, { watch: [ accountData ]})

const { data: goals } = useAsyncData('goal+overview', async () => {
    isLoadingGoal.value = true

    const res = await fetchAllGoal({
        offset: 0,
        limit: 2
    })

    isLoadingGoal.value = false

    return res.items.map(i => (goalToFundGoalCards(i))) 
})


const totalAccountBalance = computed(() => {
    let total = 0 
    let totalFreezed = 0
    let totalLocked = 0
    let totalCreditUsage = 0

    if (accountData.value) {
        for(const acc of accountData.value.accounts) {
            if (acc.type !== 'Saving' && acc.type !== 'Broking') {
                total += acc.balance
            }

            totalFreezed += acc.freezeBalance
            totalLocked += acc.lockedBalance
        }

        const creditCardAccount = accountData.value.accounts.filter(i => i.type === 'CreditCard')
        const sum = creditCardAccount.reduce((acc, account) => acc += account.detail?.detailForCreditCard?.creditUtilisation ?? 0, 0)

        totalCreditUsage = roundNumber(sum/creditCardAccount.length)
    }  

    return { 
        totalBalance: total, 
        totalFreezedBalance: totalFreezed, 
        totalLockedBalance: totalLocked,
        totalCreditUsage
    }
})


const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const slideOverQuickInvoices = overlay.create(SlideOverQuickInvoicesView)

const toast = useToast();
const onSaveAccount = async (value: EditAccount, oldValue?: AccountWithDetailType) => {
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


const onDeleteAccount = async (accountId: string) => {
    const doDelete = confirm('Voulez vous supprimer cette page');
    if (doDelete) {
        await deleteAccount(accountId);
        refreshAccounts();
    }
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

function goalStatusBadge(goal: FundCardGoal) {
    if (goal.status === 'EXPIRED') {
        return { label: 'Expiré', class: 'bg-red-100 text-red-700', progressColor: 'bg-red-500' };
    }
    const daysLeft = getDaysRemaining(goal.dueDate);
    const expectedProgress = 100;
    if (goal.percentage < expectedProgress - 15) {
        return { label: `${daysLeft} j · en retard`, class: 'bg-amber-100 text-amber-700', progressColor: 'bg-amber-500' };
    }
    return { label: `${daysLeft} j`, class: 'bg-gray-100 text-gray-600', progressColor: 'bg-primary-500' };
}

</script>

<template>
    <UiPage>
        <!-- Header avec bouton d'ajout -->
        <UiPageHeader 
            title="Mon portefeuille"
            :button="
                {
                    icon: 'i-lucide-plus',
                    label: 'Ajouter un compte'
                }
            "
            subtitle="Vue d'ensemble sur le portefeuille"
            @click-button="openAccountModal()"
        />

        <UiOverviewAccountSummary 
            v-if="!isLoadingAccount"
            :total-balance="totalAccountBalance.totalBalance"
            :disponible="availableBalance"
            :freeze="totalAccountBalance.totalFreezedBalance"
            :lock="totalAccountBalance.totalLockedBalance"
        />
        <LoadingIndicator v-else />

        <div class="grid md:grid-cols-2 grid-cols-1 gap-5">
            <div>
                <h1 class="text-lg text-gray-500 font-bold mb-5">Comptes</h1>
                <div class="grid grid-cols-2 gap-5" v-if="!isLoadingAccount">
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
            
            <div class="flex flex-col gap-4">
                <div v-if="!isKpiLoading" class="grid grid-cols-2">
                    <div>
                        <h4 class="text-gray-500 font-semibold">
                            Cashflow ce mois
                        </h4>
                        <h1 
                            :class="[
                                'font-semibold text-2xl p-2',
                                (kpi?.cashflow ?? 0) > 0 ? 'text-green-600' : 'text-red-600'
                            ]">
                            <span>{{ (kpi?.cashflow ?? 0) > 0 ? '+' : '' }}</span>
                            {{ formatCurrency(kpi?.cashflow ?? 0) }}
                        </h1>
                    </div>

                    <div>
                        <h4 class="text-gray-500 font-semibold">
                            Taux d'épargne
                        </h4>
                        <h1 class="font-semibold text-2xl p-2">{{ roundNumber(kpi?.savingRate ?? 0) }}%</h1>
                    </div>
                </div>

                <LoadingIndicator v-else />

                <div>
                    <div v-if="!isLoadingAccount">
                        <h4 class="text-gray-500 font-semibold">
                            Total credit utilisation
                        </h4>
                        <h1 :class="[
                            'font-semibold text-2xl p-2',
                            totalAccountBalance.totalCreditUsage <= 30 ? 'text-green-600' : 'text-red-600'
                        ]">{{ totalAccountBalance.totalCreditUsage }}%</h1>
                    </div>

                    <LoadingIndicator v-else />
                </div>

                <div class="flex flex-col gap-2">
                    <h1 class="font-bold">Top dépenses</h1>
                    <div v-if="!isLoadingTopSpend" class="flex flex-col gap-2">
                        <div 
                            v-for="catSpend in topSpendByCategories" 
                            :key="catSpend.categoryId"
                            class="flex items-center">
                            <div class="flex-1 flex items-center">
                                <UIcon :style="{color: catSpend.color}" :name="catSpend.icon" />
                                <span class="ml-1">{{ catSpend.title }}</span>
                            </div>
                            <span class="font-semibold">{{ formatCurrency(catSpend.spend) }}</span>
                        </div>
                    </div>
                    <div class="flex justify-center p-4" v-else-if="!isLoadingTopSpend && topSpendByCategories?.length == 0">
                        <div>
                            <UIcon name="i-lucide-mop" />
                            <p class="text-gray-500 font-semibold">Pas de dépense ce mois</p>
                        </div>
                    </div>
                    <LoadingIndicator v-else />
                </div>

                <div class="flex flex-col gap-3">
                    <div v-if="!isLoadingGoal">
                        <div v-if="goals?.length" class="flex flex-col gap-4">
                            <div v-for="goal in goals" :key="goal.id" class="flex flex-col gap-1.5">

                                <div class="flex items-center justify-between">
                                    <h3 class="font-semibold">{{ goal.title }}</h3>
                                    <span
                                        class="px-2 py-0.5 rounded-full text-[11px] font-medium"
                                        :class="goalStatusBadge(goal).class"
                                    >
                                        {{ goalStatusBadge(goal).label }}
                                    </span>
                                </div>
                            <div class="flex items-center justify-between text-xs text-gray-500">
                            <span>{{ formatCurrency(goal.currentBalance) }} / {{ formatCurrency(goal.targetAmount) }}</span>
                            <span class="font-semibold">{{ roundNumber(goal.percentage) }}%</span>
                        </div>

                        <UProgress
                            :model-value="goal.percentage"
                            :ui="{
                                indicator: goalStatusBadge(goal).progressColor 
                            }" 
                            size="sm"
                        />
                    </div>
                </div>

                <div v-else class="flex justify-center p-4">
                    <div class="text-center">
                        <UIcon name="i-lucide-target" />
                        <p class="text-gray-500 font-semibold text-sm">Aucun objectif pour l'instant</p>
                    </div>
                </div>

                </div>
                    <LoadingIndicator v-else />
                </div>
            </div>
        </div>
    </UiPage> 
</template>

<style scoped lang="scss">
</style>
