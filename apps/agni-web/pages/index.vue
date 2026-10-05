<script setup lang="ts">
import { computed } from "vue";
import { getSpendingPeriodAnalyticRange, useInProgressSpendingPeriod } from '~/composables/spendingPeriod';
import type { Account, AccountWithDetailType, EditAccount } from "~/types/ui/account";
import { getLocalTimeZone } from "@internationalized/date";
import { ModalEditAccount, SlideOverQuickInvoicesView } from "#components";
import { accountWithDetailResponseToAccountWithDetail, accountWithDetailToAccountCard, listAccountsResponseToListAccountWithDetail, toAccountBalance } from "~/mappers/account";
import { savingAnalyticResponseToSavingAnalytic } from "~/mappers/analytics";
import { goalResponseToGoal, goalToFundGoalCards } from "~/mappers/goal";
import { AccountType, getOrderAccountType } from "~/types/constants/account";
import type { CreatedRequest, ListResponse } from "~/types/api";
import type { GetAccountWithDetailResponse } from "~/types/api/account";
import type { GetSavingAnalysticResponse, GetSpendCategoryResponse } from "~/types/api/analytics";
import type { GoalResponse } from "~/types/api/goal";
import type { GetBalanceResponse } from "~/types/api/transaction";
import type { FundCardGoal } from "~/types/ui/fund";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";
import type { TotalBalanceBufferIndicator } from "~/types/ui/overview";
import { getBalanceBufferLevel } from "~/utils/getBalanceBufferLevel";

const isLoadingAccount = ref(false)
const isKpiLoading = ref(false)
const isLoadingTopSpend = ref(false)
const isLoadingGoal = ref(false)

function groupAndSortAccount(a: AccountWithDetailType, b: AccountWithDetailType) {
    const typeDiff = getOrderAccountType(a.type) - getOrderAccountType(b.type);
  
    if (typeDiff !== 0) {
        return typeDiff;
    }

  return a.title.localeCompare(b.title);
}

const { isLoading: isLoadingBalance, start: startLoadingBalance, stop: stopLoadingBalance } = useLoading()
const { data: dataTotalBalance, refresh: refresTotalBalance } = useAsyncData(
    'accounts+total+balaance',
    async () => {
        startLoadingBalance()
        const res = await ApiLinkBuilder
            .route(API_ROUTES.ACCOUNTS.TOTAL_BALANCE)
            .mapper(toAccountBalance)
            .execute()

        stopLoadingBalance()

        return res
    }
)

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
        // dateFrom.setMonth(dateFrom.getMonth() - 4)

        // const balancesByPeriod = await Promise.all(
        //     accIds.map(id =>
        //         ApiLinkBuilder.route<GetBalanceResponse[]>(API_ROUTES.INVOICES.GET_BALANCES_BY_PERIOD).query({
        //             period: 'Month',
        //             interval: 1,
        //             dateFrom: dateFrom.toISOString(),
        //             accountIds: [id]
        //         }).execute()
        //     )
        // )

        isLoadingAccount.value = false

        return {
            accounts: res.items.sort((a, b) => groupAndSortAccount(a, b)),
            balanceHistories: accIds.map((id, index) => ({
                id,
                histories: [] // balancesByPeriod[index]?.map(i => i.balance) ?? []
            }))
        }
    }
)

const { data: inProgressSpendingPeriod } = await useInProgressSpendingPeriod()

const analyticRange = computed(() => getSpendingPeriodAnalyticRange(inProgressSpendingPeriod.value))

const { data: kpi } = useAsyncData('cashflow+savingrates', async () => {
    isKpiLoading.value = true

    const range = analyticRange.value

    const [currentBalance, savingBalance] = await Promise.all([        
        ApiLinkBuilder.route<GetBalanceResponse>(API_ROUTES.INVOICES.GET_BALANCES).query({
            startDate: range.startDate,
            endDate: range.endDate,
            isFreeze: false
        }).execute(),
        ApiLinkBuilder.route<GetSavingAnalysticResponse>(API_ROUTES.ANALYTICS.SAVINGS).query({
            period: range.period,
            interval: range.interval,
            startDate: range.startDate,
        }).mapper(savingAnalyticResponseToSavingAnalytic).execute()
    ])

    isKpiLoading.value = false

    return {
        cashflow: currentBalance.income - currentBalance.spend,
        savingRate: (savingBalance.savingRates[0] ?? 0.0) * 100
    }
}, { watch: [ accountData ]})

const { data: topSpendByCategories } = useAsyncData('top-spend-categories', async () => {
    isLoadingTopSpend.value = true

    const res = await ApiLinkBuilder.route<ListResponse<GetSpendCategoryResponse>>(API_ROUTES.ANALYTICS.SPEND_CATEGORIES).query({
        period: analyticRange.value.period,
        interval: analyticRange.value.interval,
        startDate: analyticRange.value.startDate,
        offset: 0,
        limit: 0,
        queryAll: true
    }).execute()

    isLoadingTopSpend.value = false

    return res.items
            .map(item => ({ 
                ...item,
                spend: item.spends?.at(-1) ?? 0,
            }))
            .filter(i => i.spend > 0).sort((a, b) => b.spend - a.spend).slice(0, 4)

}, { watch: [ accountData ]})

const { data: goals } = useAsyncData('goal+overview', async () => {
    isLoadingGoal.value = true

    const res = await ApiLinkBuilder.route<ListResponse<GoalResponse>>(API_ROUTES.GOALS.GET_GOALS).query({offset: 0, limit: 2}).execute();
    const items = res.items.map(i => goalResponseToGoal(i));

    isLoadingGoal.value = false

    return items.map(i => (goalToFundGoalCards(i))) 
})


const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const slideOverQuickInvoices = overlay.create(SlideOverQuickInvoicesView)

const toast = useToast();
const onSaveAccount = async (value: EditAccount, oldValue?: AccountWithDetailType) => {
    try {
        if (oldValue)
            await ApiLinkBuilder.route(API_ROUTES.ACCOUNTS.UPDATE_ACCOUNT).params({id: oldValue.id}).body({
                title: value.title,
                type : value.type,
                color: value.color,
                detail: {
                    contributionType: value.contributionType,
                    managementAccount: value.managementType,
                    creditLimit: value.creditLimit,
                    invoiceDate: value.invoiceDate?.toDate(getLocalTimeZone()).toISOString()
                }
            }).execute();
        else 
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.ACCOUNTS.CREATE_ACCOUNT).body({
                title: value.title,
                type : value.type,
                color: value.color,
                detail: {
                    contributionType: value.contributionType,
                    managementAccount: value.managementType,
                    creditLimit: value.creditLimit,
                    invoiceDate: value.invoiceDate?.toDate(getLocalTimeZone()).toISOString()
                }
            }).execute();
        
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
        account = await ApiLinkBuilder.route<GetAccountWithDetailResponse>(API_ROUTES.ACCOUNTS.GET_ACCOUNT).params({id: accountId}).query({withDetail: true}).mapper(accountWithDetailResponseToAccountWithDetail).execute();
    }
        
    modalAccount.open({
        account: account,
        onSubmit: onSaveAccount 
    }); 
}


const onDeleteAccount = async (accountId: string) => {
    const doDelete = confirm('Voulez vous supprimer cette page');
    if (doDelete) {
        await ApiLinkBuilder.route(API_ROUTES.ACCOUNTS.DELETE_ACCOUNT).params({id: accountId}).execute();
        refreshAccounts();
    }
}



const openTransactionViews = async (accountId: string) => {
    try {
        const instance = slideOverQuickInvoices.open({
            accountId: accountId,
            onClose: (refresh) => {
                if (refresh) {
                    refreshAccounts()
                    refresTotalBalance()
                } 
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

const balanceBufferIndicator = computed<TotalBalanceBufferIndicator>(() => {
    const buffer = dataTotalBalance.value?.buffer.baseBufferAmount ?? 0
    const totalBalance = dataTotalBalance.value?.totalBalance ?? 0
    const diffBalance = dataTotalBalance.value?.buffer.currentBalanceBuffer ?? totalBalance - buffer
    const projectedBuffer = dataTotalBalance.value?.buffer.projectedBuffer ?? 0
    const projectedBufferByBalance = dataTotalBalance.value?.buffer.projectedBufferByBalance ?? projectedBuffer - buffer
    const isUnderBuffer = diffBalance < 0
    const missingBalance = Math.abs(diffBalance)
    const coverage = buffer > 0
        ? Math.max(0, Math.min(100, roundNumber((totalBalance / buffer) * 100)))
        : (totalBalance > 0 ? 100 : 0)
    const estimateCoverage = buffer > 0
        ? Math.max(0, Math.min(100, roundNumber((projectedBuffer / buffer) * 100)))
        : (projectedBuffer > 0 ? 100 : 0)

    return {
        buffer,
        diffBalance,
        projectedBuffer,
        projectedBufferByBalance,
        estimateCoverage,
        isUnderBuffer,
        estimateLevel: getBalanceBufferLevel(buffer, projectedBuffer),
        coverage,
        level: getBalanceBufferLevel(buffer, totalBalance),
        description: isUnderBuffer
            ? `Il manque ${formatCurrency(missingBalance)} pour atteindre le buffer`
            : `Buffer atteint avec ${formatCurrency(diffBalance)} de marge`
    }
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
            v-if="!isLoadingBalance"
            :indicator-balance-buffer="balanceBufferIndicator"
            :total-balance="dataTotalBalance?.totalBalance ?? 0"
            :disponible="dataTotalBalance?.totalAvailable ?? 0"
            :freeze="dataTotalBalance?.totalFreeze ?? 0"
            :lock="dataTotalBalance?.totalLock ?? 0"
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
                <UiOverviewSpendingPeriodRemain />

                <div v-if="!isKpiLoading" class="grid grid-cols-2">
                    <div>
                        <h4 class="text-gray-500 font-semibold">
                            {{ analyticRange.isSpendingPeriod ? 'Cashflow de la periode' : 'Cashflow ce mois' }}
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
                            (dataTotalBalance?.totalCreditUtilization ?? 0) <= 30 ? 'text-green-600' : 'text-red-600'
                        ]">{{ roundNumber(dataTotalBalance?.totalCreditUtilization ?? 0) }}%</h1>
                    </div>

                    <LoadingIndicator v-else />
                </div>

                <div class="flex flex-col gap-2">
                    <h1 class="font-bold">
                        {{ analyticRange.isSpendingPeriod ? 'Top dépenses de la periode' : 'Top dépenses' }}
                    </h1>
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
