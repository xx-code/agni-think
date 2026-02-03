<script setup lang="ts">
import useAccountsWitPastBalance, { ALL_ACCOUNT_ID } from '~/composables/accounts/useAccountsWithPastBalance'
import useAnalyseBudgetRules from '~/composables/analytics/useBudgetRules'
import useCashflow from '~/composables/analytics/useCashflow'
import useAnalyticIncomes from '~/composables/analytics/useIncomes'
import useAnalyticSavings from '~/composables/analytics/useSaving'
import useAnalyticSpends from '~/composables/analytics/useSpends'
import useBudgets from '~/composables/budgets/useBudgets'
import useCategories from '~/composables/categories/useCategories'
import useSaveGoals from '~/composables/goals/useSaveGoals'
import useTags from '~/composables/tags/useTags'
import useTransactionPagination from '~/composables/transactions/useTransactionPagination'
import type { FilterTransactionQuery } from '~/types/api/transaction'
import { formatBudgetDataForChart } from '~/utils/formatBudgetDataForChart'
import { getListTime } from '~/utils/getListTime'

type CardInfo = {
    amount: number
    description: string
    cardInfo: string
    isPositif: boolean
}

type DetailModalType = 'income' | 'spend' | 'cashflow' | 'saving' | 'budget' | 'goals' | null

const calendarSelection = reactive<{
    period: 'Year' | 'Month' | 'Week',
    periodTime: number,
    showNumber: number
}>({
    period: 'Month',
    periodTime: 1,
    showNumber: 6  
})

// Modal state
const isDetailModalOpen = ref(false)
const currentDetailType = ref<DetailModalType>(null)
const modalTitle = ref('')
const modalData = ref<any>(null)

const {data: categories } = useCategories({
    limit: 0, offset: 0, queryAll: true
})
const {data: tags } = useTags({
    limit: 0, offset: 0, queryAll: true
})

const { data: cashflow } = useCashflow(calendarSelection) 
const { data: analyticSpendAllocation } = useAnalyseBudgetRules(calendarSelection)  
const { data:analyticIncomes } = useAnalyticIncomes(calendarSelection)
const { data:analyticSaving } = useAnalyticSavings(calendarSelection)
const { data:analyticSpend } = useAnalyticSpends(calendarSelection)

const incomeCardInfo = computed<CardInfo>(() => {
    let currentIncome = 0
    if (analyticIncomes.value)
        currentIncome = analyticIncomes.value.incomes[analyticIncomes.value.incomes.length - 1] 

    let lastIncome = 0 
    if (analyticIncomes.value && analyticIncomes.value.incomes.length > 2)
        lastIncome = analyticIncomes.value.incomes[analyticIncomes.value.incomes.length - 2]

    const percent = computePercentage(lastIncome, Math.abs(lastIncome - currentIncome))
    const lastDesc = analyticIncomes.value?.incomesByDescription[analyticIncomes.value.incomesByDescription.length - 1]

    return {
        amount: currentIncome,
        description: "Source: " +  lastDesc?.map(i => i.label + ' ' + formatCurrency(i.income)).join(' . '),
        cardInfo: `%${percent.toFixed(2)} ${calendarSelection.period}/${calendarSelection.period}`,
        isPositif: currentIncome > lastIncome,
    }
})

const spendCardInfo = computed<CardInfo>(() => {
    let currentSpend = 0
    if (analyticSpend.value)
        currentSpend = analyticSpend.value.totalSpends[analyticSpend.value.totalSpends.length - 1] 

    let lastSpend = 0 
    if (analyticSpend.value && analyticSpend.value.totalSpends.length > 2)
        lastSpend = analyticSpend.value.totalSpends[analyticSpend.value.totalSpends.length - 2]

    const percent = computePercentage(lastSpend, Math.abs(lastSpend - currentSpend) )

    let lastAllocations = ''
    if (analyticSpendAllocation.value && analyticSpendAllocation.value?.distributionSpends.length > 0)
        lastAllocations = analyticSpendAllocation.value?.distributionSpends[0].map(i => `${i.transactionType} %${i.value}`).join(' . ') ?? ''

    return {
        amount: currentSpend,
        description: lastAllocations,
        cardInfo: `%${percent.toFixed(2)} ${calendarSelection.period}/${calendarSelection.period}`, 
        isPositif: currentSpend < lastSpend 
    } 
})

const cashflowCardInfo = computed<CardInfo>(() => {
    let currentCashFlow = 0
    if (cashflow.value)
        currentCashFlow = cashflow.value.incomes[cashflow.value.incomes.length - 1] - cashflow.value.spends[cashflow.value.spends.length - 1]

    let lastCashFlows: number[] = []
    let isPositif = true
    if (cashflow.value && cashflow.value.incomes.length > 1 && cashflow.value.spends.length > 1) {
        const stop = cashflow.value.incomes.length > 3 ? cashflow.value.incomes.length - 3 : 1
        for(let i = cashflow.value.incomes.length - 1; i > stop; i--) {
            if ((cashflow.value.incomes[i] - cashflow.value.spends[i]) < 0)
                isPositif = false
        
            lastCashFlows.push(cashflow.value.incomes[i] - cashflow.value.spends[i])
        }
    }

    const mean = Number((lastCashFlows.reduce(((acc, i) => acc + i), 0)/3).toFixed(2))

    return {
        amount: currentCashFlow,
        cardInfo: isPositif  ?`3 ${calendarSelection.period} positif` : `un ou plusieurs ${calendarSelection.period} negatif`,
        description: `Moyen ${formatCurrency(mean)}`,
        isPositif: isPositif 
    }
})

const savingCardInfo = computed<CardInfo>(() => {
    let currentSavingRate = 0
    let currentSaving = 0
    if (analyticSaving.value) {
        currentSavingRate = analyticSaving.value.savingRates[analyticSaving.value.savingRates.length - 1]
        currentSaving = analyticSaving.value.savings[analyticSaving.value.savings.length - 1]
    }

    return {
        amount: currentSavingRate * 100,
        description: `Épargne totale ce mois: ${formatCurrency(currentSaving)}`,
        cardInfo: `Objectif ${(currentSavingRate * 100).toFixed(2)}% >= 10%`,
        isPositif: (currentSavingRate * 100) > 10
    }
})

const labels = computed(() => {
    const values = getListTime(
        calendarSelection.period, {
            count: calendarSelection.showNumber,
            spacing: calendarSelection.periodTime
        })

    return values
})

const optionsChart = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
})) 

const dataChart = computed(() => ({
    labels: labels.value, 
    datasets: [{
        label: 'Gains',
        data: cashflow.value?.incomes || [],
        backgroundColor: '#10b981',
        borderWidth: 1,
    }, {
        label: 'Dépense',
        data: cashflow.value?.spends || [],
        backgroundColor: '#ef4444',
        borderWidth: 1
    }],
}))

const getCategory = (id: string) => {
    const cat = categories.value?.items.find(i => i.id === id)
    return cat
}
const getTag = (id: string) => {
    const tag = tags.value?.items.find(i => i.id === id)
    return tag
}

const bestCategories = computed(() => {
    if (analyticSpend.value && analyticSpend.value.spendByCategories.length > 0 )  {
        const spendLength = analyticSpend.value.spendByCategories.length
        const length = analyticSpend.value.spendByCategories[spendLength - 1].length
        const results = []
        for(let i = 0; i < length; i++) {
            if (i > 4)
                break
            const spends = analyticSpend.value.spendByCategories[spendLength - 1].sort((a, b) => b.spend - a.spend)
            results.push(spends[i])
        }

        return results
    }

    return []
})

const spendLabels = computed<{label: string, color: string}[]>(() => {
    if (bestCategories.value.length > 0 )  {
        const categories = bestCategories.value.map(i => getCategory(i.categoryId)).filter(i => i !== undefined)

        return categories.map(i => ({label: i.title, color: i.color ?? "#b2bac4"}))
    }

    return []
})

const dataSpendChart = computed(() => ({
    labels: spendLabels.value.map(i => i.label),
    datasets: [{
        label: 'Dépenses',
        data: bestCategories.value.map(i => i.spend) || [],
        backgroundColor: spendLabels.value.map(i => i.color),
        borderWidth: 1,
    }],
}))

const {data: budgets} = useBudgets({
    limit: 0, offset: 0, queryAll: false
})

const budgetChart = computed(() => {
    return formatBudgetDataForChart(budgets.value?.items)
});

const {data:accounts, error:accountError, refresh:accountRefresh} = useAccountsWitPastBalance({ 
    period: 'Month', periodTime: 1, offset: 0, limit: 0, queryAll: true
});

const transactionAccountSelected = ref(ALL_ACCOUNT_ID)
const accountsChecked: Ref<{id: string, checked: boolean}[]> = ref([])
const items = computed(() => {
    if (accounts.value)
        return accounts.value.items.map(acc => (
        {
            label: acc.title,
            type: 'checkbox' as const,
            checked: accountsChecked.value.find(tran => tran.id === acc.id)?.checked,
            onUpdateChecked(checked: boolean) {
                const index = accountsChecked.value.findIndex(e => e.id == acc.id)
                if (index !== -1)
                    accountsChecked.value[index].checked = checked 
            },
            onSelect(e: Event) {
                e.preventDefault()
            }
        }    )) 
    return []
})

const paramsTransaction = reactive<FilterTransactionQuery>({
    offset: 0, limit: 5, status: 'Pending'})
const {data: transactions} = useTransactionPagination(paramsTransaction);
const {data: goals} =  useSaveGoals({limit: 4, offset: 0, sortSense: 'desc', sortBy: 'balance', queryAll: false});
const displayGoals = computed(() => {
    return goals.value?.items 
})

const dateDisplayed = ref("Mois");
const listTypeDateDisplay =computed(() => (
[
    {
        label: 'Bi-hebdomadaire' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            calendarSelection.periodTime = 2
            dateDisplayed.value = "Bi-hebdomadaire"
            calendarSelection.period = "Week"
        } 
    },
    {
        label: 'Mois' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            calendarSelection.period = "Month"
            calendarSelection.periodTime = 1
            dateDisplayed.value = "Mois"
        } 
    },
    {
        label: 'Semestre' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            calendarSelection.period = "Month"
            calendarSelection.periodTime = 2
            dateDisplayed.value = "Semestre"
        } 
    },
    {
        label: 'Trimestre' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            calendarSelection.period = "Month"
            calendarSelection.periodTime = 3
            dateDisplayed.value = "Trimestre"
        } 
    },
    {
        label: 'Année' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            calendarSelection.period = "Year"
            calendarSelection.periodTime = 1
            dateDisplayed.value = "Année"
        } 
    }
]
)) 

// Modal functions
function openDetailModal(type: DetailModalType) {
    currentDetailType.value = type
    
    switch(type) {
        case 'income':
            modalTitle.value = 'Détails des Revenus'
            modalData.value = {
                current: incomeCardInfo.value.amount,
                history: analyticIncomes.value?.incomes || [],
                byDescription: analyticIncomes.value?.incomesByDescription || [],
                labels: labels.value
            }
            break
        case 'spend':
            modalTitle.value = 'Détails des Dépenses'
            modalData.value = {
                current: spendCardInfo.value.amount,
                history: analyticSpend.value?.totalSpends || [],
                byCategory: analyticSpend.value?.spendByCategories || [],
                distribution: analyticSpendAllocation.value?.distributionSpends || [],
                labels: labels.value
            }
            break
        case 'cashflow':
            modalTitle.value = 'Détails du Cashflow'
            modalData.value = {
                current: cashflowCardInfo.value.amount,
                incomes: cashflow.value?.incomes || [],
                spends: cashflow.value?.spends || [],
                labels: labels.value
            }
            break
        case 'saving':
            modalTitle.value = 'Détails de l\'Épargne'
            modalData.value = {
                currentRate: savingCardInfo.value.amount,
                savings: analyticSaving.value?.savings || [],
                savingRates: analyticSaving.value?.savingRates || [],
                labels: labels.value
            }
            break
        case 'budget':
            modalTitle.value = 'Détails des Budgets'
            modalData.value = {
                budgets: budgets.value?.items || [],
                chart: budgetChart.value
            }
            break
        case 'goals':
            modalTitle.value = 'Objectifs d\'Épargne'
            modalData.value = {
                goals: displayGoals.value || []
            }
            break
    }
    
    isDetailModalOpen.value = true
}

function closeDetailModal() {
    isDetailModalOpen.value = false
    currentDetailType.value = null
    modalData.value = null
}

watchEffect(() => {
    if (accounts.value)
        accountsChecked.value = accounts.value?.items.map(acc => ({id: acc.id, checked: true}))
})

</script>

<template>
    <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
        <div class="max-w-7xl mx-auto p-4 md:p-6 lg:p-8">
            <!-- Header Section -->
            <div class="mb-8">
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <h1 class="text-3xl font-bold text-gray-900 mb-2">
                            Tableau de Bord
                        </h1>
                        <p class="text-gray-600 text-sm">
                            Vue d'ensemble de vos finances
                        </p>
                    </div>
                    <div class="flex gap-2">
                        <UDropdownMenu :items="listTypeDateDisplay">
                            <UButton size="lg" variant="outline" color="neutral" class="shadow-sm">
                                <UIcon name="i-lucide-calendar" class="mr-2" />
                                {{ dateDisplayed }}
                            </UButton>
                        </UDropdownMenu>
                        <UDropdownMenu :items="items" :ui="{content: 'w-48'}">
                            <UButton icon="i-lucide-plus" color="primary" size="lg" variant="solid" class="shadow-lg">
                                Ajouter carte
                            </UButton>
                        </UDropdownMenu>
                    </div>
                </div>
            </div>

            <!-- Summary Cards -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                <div class="cursor-pointer transform transition-all hover:scale-105" @click="openDetailModal('income')">
                    <CardResumeAnalytics 
                        title="Revenu net du mois"
                        :amount="incomeCardInfo.amount"
                        :chip-info="incomeCardInfo.cardInfo"
                        :is-percentage="false"
                        :description="incomeCardInfo.description"
                        :is-positive="incomeCardInfo.isPositif"
                        class="h-full shadow-lg hover:shadow-xl transition-shadow" />
                </div>
                
                <div class="cursor-pointer transform transition-all hover:scale-105" @click="openDetailModal('spend')">
                    <CardResumeAnalytics 
                        title="Dépenses totales"
                        :amount="spendCardInfo.amount"
                        :chip-info="spendCardInfo.cardInfo"
                        :is-percentage="false"
                        :description="spendCardInfo.description"
                        :is-positive="spendCardInfo.isPositif"
                        class="h-full shadow-lg hover:shadow-xl transition-shadow" />
                </div>
                
                <div class="cursor-pointer transform transition-all hover:scale-105" @click="openDetailModal('cashflow')">
                    <CardResumeAnalytics 
                        title="Cashflow net"
                        :amount="cashflowCardInfo.amount"
                        :chip-info="cashflowCardInfo.cardInfo"
                        :is-percentage="false"
                        :description="cashflowCardInfo.description"
                        :is-positive="cashflowCardInfo.isPositif"
                        class="h-full shadow-lg hover:shadow-xl transition-shadow" />
                </div>
                
                <div class="cursor-pointer transform transition-all hover:scale-105" @click="openDetailModal('saving')">
                    <CardResumeAnalytics 
                        title="Taux d'épargne"
                        :amount="savingCardInfo.amount"
                        :is-percentage="true"
                        :chip-info="savingCardInfo.cardInfo"
                        :description="savingCardInfo.description"
                        :is-positive="savingCardInfo.isPositif"
                        class="h-full shadow-lg hover:shadow-xl transition-shadow" />
                </div>
            </div>

            <!-- Charts Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
                <!-- Money Flow Chart -->
                <div class="lg:col-span-2 bg-white rounded-2xl shadow-lg border border-gray-200 overflow-hidden">
                    <div class="p-4 border-b border-gray-100 flex justify-between items-center">
                        <CustomCardTitle title="Money Flow" />
                        <UButton 
                            icon="i-lucide-maximize-2" 
                            variant="ghost" 
                            color="neutral" 
                            size="sm"
                            @click="openDetailModal('cashflow')" />
                    </div>
                    <div class="p-4">
                        <div class="h-72">
                            <BarChart :data="dataChart" :options="optionsChart" />
                        </div>
                    </div>
                </div>

                <!-- Budgets Chart -->
                <div class="bg-white rounded-2xl shadow-lg border border-gray-200 overflow-hidden cursor-pointer hover:shadow-xl transition-shadow" @click="openDetailModal('budget')">
                    <div class="p-4 border-b border-gray-100 flex justify-between items-center">
                        <CustomCardTitle title="Budgets" />
                        <UButton 
                            icon="i-lucide-external-link" 
                            variant="ghost" 
                            color="neutral" 
                            size="sm" />
                    </div>
                    <div class="p-4">
                        <div class="h-72 flex justify-center items-center">
                            <DoughnutChart :data="budgetChart" :options="optionsChart"/>
                        </div>
                    </div>
                </div>

                <!-- Spending by Category -->
                <div class="lg:col-span-2 bg-white rounded-2xl shadow-lg border border-gray-200 overflow-hidden">
                    <div class="p-4 border-b border-gray-100 flex justify-between items-center">
                        <CustomCardTitle title="Dépenses par catégories" />
                        <UButton 
                            icon="i-lucide-maximize-2" 
                            variant="ghost" 
                            color="neutral" 
                            size="sm"
                            @click="openDetailModal('spend')" />
                    </div>
                    <div class="p-4">
                        <div class="h-72">
                            <BarChart :data="dataSpendChart" :options="optionsChart" />
                        </div>
                    </div>
                </div>

                <!-- Savings Goals -->
                <div class="bg-white rounded-2xl shadow-lg border border-gray-200 overflow-hidden cursor-pointer hover:shadow-xl transition-shadow" @click="openDetailModal('goals')">
                    <div class="p-4 border-b border-gray-100 flex justify-between items-center">
                        <CustomCardTitle title="Buts d'épargne" />
                        <UButton 
                            icon="i-lucide-external-link" 
                            variant="ghost" 
                            color="neutral" 
                            size="sm" />
                    </div>
                    <div class="p-4">
                        <div class="flex flex-col gap-3">
                            <div v-for="goal in displayGoals" :key="goal.id">
                                <BarBudgetInfo 
                                    :id="goal.id" 
                                    :title="goal.title" 
                                    :target-amount="goal.target" 
                                    :amount="goal.balance" />
                            </div>
                            <div v-if="!displayGoals || displayGoals.length === 0" class="text-center py-8">
                                <UIcon name="i-lucide-target" class="text-gray-300 text-4xl mb-2" />
                                <p class="text-gray-500 text-sm">Aucun objectif d'épargne</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Detail Modal -->
        <UModal v-model="isDetailModalOpen" :ui="{ wrapper: 'max-w-4xl' }">
            <div class="p-6">
                <div class="flex justify-between items-center mb-6">
                    <h2 class="text-2xl font-bold text-gray-900">{{ modalTitle }}</h2>
                    <UButton 
                        icon="i-lucide-x" 
                        color="neutral" 
                        variant="ghost" 
                        @click="closeDetailModal" />
                </div>

                <!-- Income Details -->
                <div v-if="currentDetailType === 'income' && modalData" class="space-y-6">
                    <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-6 border border-green-200">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-sm text-green-700 mb-1">Revenu Actuel</p>
                                <p class="text-4xl font-bold text-green-900">{{ formatCurrency(modalData.current) }}</p>
                            </div>
                            <div class="p-4 bg-green-200 rounded-full">
                                <UIcon name="i-lucide-trending-up" class="text-green-700 text-3xl" />
                            </div>
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Historique des revenus</h3>
                        <div class="h-64">
                            <BarChart 
                                :data="{
                                    labels: modalData.labels,
                                    datasets: [{
                                        label: 'Revenus',
                                        data: modalData.history,
                                        backgroundColor: '#10b981',
                                        borderWidth: 1
                                    }]
                                }" 
                                :options="{ responsive: true, maintainAspectRatio: false }" />
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Sources de revenus</h3>
                        <div class="space-y-3">
                            <div v-for="(sources, index) in modalData.byDescription" :key="index" class="space-y-2">
                                <p class="text-sm font-medium text-gray-600">Période {{ (index as number) + 1 }}</p>
                                <div v-for="source in sources" :key="source.label" class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                    <span class="text-gray-700">{{ source.label }}</span>
                                    <span class="font-semibold text-green-600">{{ formatCurrency(source.income) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Spend Details -->
                <div v-if="currentDetailType === 'spend' && modalData" class="space-y-6">
                    <div class="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-6 border border-red-200">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-sm text-red-700 mb-1">Dépenses Actuelles</p>
                                <p class="text-4xl font-bold text-red-900">{{ formatCurrency(modalData.current) }}</p>
                            </div>
                            <div class="p-4 bg-red-200 rounded-full">
                                <UIcon name="i-lucide-trending-down" class="text-red-700 text-3xl" />
                            </div>
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Historique des dépenses</h3>
                        <div class="h-64">
                            <BarChart 
                                :data="{
                                    labels: modalData.labels,
                                    datasets: [{
                                        label: 'Dépenses',
                                        data: modalData.history,
                                        backgroundColor: '#ef4444',
                                        borderWidth: 1
                                    }]
                                }" 
                                :options="{ responsive: true, maintainAspectRatio: false }" />
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Top 5 catégories</h3>
                        <div class="space-y-3">
                            <div v-for="(category, index) in bestCategories" :key="index" class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                                <div class="flex items-center gap-3">
                                    <div 
                                        class="w-10 h-10 rounded-full flex items-center justify-center"
                                        :style="{ backgroundColor: `${getCategory(category.categoryId)?.color}22`, border: `2px solid ${getCategory(category.categoryId)?.color}` }">
                                        <UIcon 
                                            :name="getCategory(category.categoryId)?.icon || 'i-lucide-circle'" 
                                            :style="{ color: getCategory(category.categoryId)?.color }" />
                                    </div>
                                    <span class="font-medium text-gray-800">{{ getCategory(category.categoryId)?.title }}</span>
                                </div>
                                <span class="font-bold text-red-600">{{ formatCurrency(category.spend) }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Cashflow Details -->
                <div v-if="currentDetailType === 'cashflow' && modalData" class="space-y-6">
                    <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6 border border-blue-200">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-sm text-blue-700 mb-1">Cashflow Actuel</p>
                                <p class="text-4xl font-bold" :class="modalData.current >= 0 ? 'text-green-900' : 'text-red-900'">
                                    {{ formatCurrency(modalData.current) }}
                                </p>
                            </div>
                            <div class="p-4 bg-blue-200 rounded-full">
                                <UIcon name="i-lucide-arrow-right-left" class="text-blue-700 text-3xl" />
                            </div>
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Évolution Revenus vs Dépenses</h3>
                        <div class="h-80">
                            <BarChart 
                                :data="{
                                    labels: modalData.labels,
                                    datasets: [
                                        {
                                            label: 'Revenus',
                                            data: modalData.incomes,
                                            backgroundColor: '#10b981',
                                            borderWidth: 1
                                        },
                                        {
                                            label: 'Dépenses',
                                            data: modalData.spends,
                                            backgroundColor: '#ef4444',
                                            borderWidth: 1
                                        }
                                    ]
                                }" 
                                :options="{ responsive: true, maintainAspectRatio: false }" />
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div class="bg-green-50 rounded-xl p-4 border border-green-200">
                            <p class="text-sm text-green-700 mb-1">Total Revenus</p>
                            <p class="text-2xl font-bold text-green-900">
                                {{ formatCurrency(modalData.incomes.reduce((a: number, b: number) => a + b, 0)) }}
                            </p>
                        </div>
                        <div class="bg-red-50 rounded-xl p-4 border border-red-200">
                            <p class="text-sm text-red-700 mb-1">Total Dépenses</p>
                            <p class="text-2xl font-bold text-red-900">
                                {{ formatCurrency(modalData.spends.reduce((a: number, b: number) => a + b, 0)) }}
                            </p>
                        </div>
                    </div>
                </div>

                <!-- Saving Details -->
                <div v-if="currentDetailType === 'saving' && modalData" class="space-y-6">
                    <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-6 border border-purple-200">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-sm text-purple-700 mb-1">Taux d'Épargne Actuel</p>
                                <p class="text-4xl font-bold text-purple-900">{{ modalData.currentRate.toFixed(2) }}%</p>
                            </div>
                            <div class="p-4 bg-purple-200 rounded-full">
                                <UIcon name="i-lucide-piggy-bank" class="text-purple-700 text-3xl" />
                            </div>
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Évolution de l'épargne</h3>
                        <div class="h-64">
                            <BarChart 
                                :data="{
                                    labels: modalData.labels,
                                    datasets: [{
                                        label: 'Épargne',
                                        data: modalData.savings,
                                        backgroundColor: '#8b5cf6',
                                        borderWidth: 1
                                    }]
                                }" 
                                :options="{ responsive: true, maintainAspectRatio: false }" />
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Taux d'épargne par période</h3>
                        <div class="space-y-2">
                            <div v-for="(rate, index) in modalData.savingRates" :key="index" class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                <span class="text-gray-700">{{ modalData.labels[index] }}</span>
                                <span class="font-semibold" :class="rate >= 0.1 ? 'text-green-600' : 'text-red-600'">
                                    {{ (rate * 100).toFixed(2) }}%
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Budget Details -->
                <div v-if="currentDetailType === 'budget' && modalData" class="space-y-6">
                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Répartition des budgets</h3>
                        <div class="h-80 flex justify-center items-center">
                            <DoughnutChart 
                                :data="modalData.chart" 
                                :options="{ responsive: true, maintainAspectRatio: false }" />
                        </div>
                    </div>

                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Liste des budgets</h3>
                        <div class="space-y-3">
                            <div v-for="budget in modalData.budgets" :key="budget.id" class="p-4 bg-gray-50 rounded-lg">
                                <div class="flex justify-between items-center mb-2">
                                    <span class="font-medium text-gray-800">{{ budget.title }}</span>
                                    <span class="font-semibold text-blue-600">{{ formatCurrency(budget.amount) }}</span>
                                </div>
                                <div class="w-full bg-gray-200 rounded-full h-2">
                                    <div 
                                        class="bg-blue-600 h-2 rounded-full transition-all"
                                        :style="{ width: `${Math.min((budget.spent / budget.amount) * 100, 100)}%` }">
                                    </div>
                                </div>
                                <p class="text-sm text-gray-600 mt-1">
                                    {{ formatCurrency(budget.spent) }} / {{ formatCurrency(budget.amount) }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Goals Details -->
                <div v-if="currentDetailType === 'goals' && modalData" class="space-y-6">
                    <div class="bg-white rounded-xl p-6 border border-gray-200">
                        <h3 class="text-lg font-semibold mb-4 text-gray-900">Vos objectifs d'épargne</h3>
                        <div class="space-y-4">
                            <div v-for="goal in modalData.goals" :key="goal.id" class="p-5 bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl border border-blue-200">
                                <div class="flex justify-between items-center mb-3">
                                    <h4 class="font-semibold text-gray-900 text-lg">{{ goal.title }}</h4>
                                    <span class="text-sm font-medium text-gray-600">
                                        {{ ((goal.balance / goal.target) * 100).toFixed(1) }}%
                                    </span>
                                </div>
                                <div class="mb-3">
                                    <BarBudgetInfo 
                                        :id="goal.id" 
                                        :title="goal.title" 
                                        :target-amount="goal.target" 
                                        :amount="goal.balance" />
                                </div>
                                <div class="flex justify-between text-sm">
                                    <span class="text-gray-600">Actuel: <span class="font-semibold text-blue-600">{{ formatCurrency(goal.balance) }}</span></span>
                                    <span class="text-gray-600">Objectif: <span class="font-semibold text-purple-600">{{ formatCurrency(goal.target) }}</span></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </UModal>
    </div>
</template>

<style lang="scss" scoped>
// Keep any custom styles you need
</style>