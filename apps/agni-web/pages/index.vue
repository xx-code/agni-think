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

const calendarSelection = reactive<{
    period: 'Year' | 'Month' | 'Week',
    periodTime: number,
    showNumber: number
}>({
    period: 'Month',
    periodTime: 1,
    showNumber: 6  
})

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

    const mean = Number((lastCashFlows.reduce(((acc, i) => acc + i), 0)/3).toFixed(2))// Change mean mobile

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

    // let lastSavingRate = 0
    // if (analyticSaving.value && analyticSaving.value.savingRates.length > 1) {
    //     lastSavingRate = analyticSaving.value.savingRates[analyticSaving.value.savingRates.length - 2]
    // }

    return {
        amount: currentSavingRate * 100,
        description: `Epagne total se mois: ${formatCurrency(currentSaving)}`,
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
})) 
const dataChart = computed(() => ({
    labels: labels.value, 
    datasets: [{
        label: 'Gains',
        data: cashflow.value?.incomes || [],
        backgroundColor: '#6655d7',
        borderWidth: 1,
    }, {
        label: 'Depense',
        data: cashflow.value?.spends || [],
        backgroundColor: 'rgba(103, 85, 215, 0.1)',
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
        label: '',
        data: bestCategories.value.map(i => i.spend) || [],
        backgroundColor: spendLabels.value.map(i => i.color),
        borderWidth: 1,
    }],
}))

const {data: budgets} = useBudgets({
    limit: 0, offset: 0, queryAll: false
})

const budgetChart = computed(() => {
    return formatBudgetDataForChart(budgets.value?.items) // TODO: review
});

const {data:accounts, error:accountError, refresh:accountRefresh} = useAccountsWitPastBalance({ 
    period: 'Month', periodTime: 1, offset: 0, limit: 0, queryAll: true
});

const transactionAccountSelected = ref(ALL_ACCOUNT_ID)
const accountsChecked: Ref<{id: string, checked: boolean}[]> = ref([]) // TODO: Review
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
        label: 'Annee' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            calendarSelection.period = "Year"
            calendarSelection.periodTime = 1
            dateDisplayed.value = "Annee"
        } 
    }
]
)) 

/*const listAccount = computed(() => {
    if (accounts.value)
        return accounts.value
    return []
})*/

watchEffect(() => {
    if (accounts.value)
        accountsChecked.value = accounts.value?.items.map(acc => ({id: acc.id, checked: true}))
})

</script>

<template>
    <div>
        <div class="option-diplay-container">
            <div class="flex gap-2">
                <UDropdownMenu :items="listTypeDateDisplay">
                    <UButton size="xl" variant="outline" color="neutral">
                        <UIcon name="i-lucide-calendar"  />
                    </UButton>
                </UDropdownMenu>
                <UButton  size="xl" variant="outline" color="neutral">
                    {{ dateDisplayed }}
                </UButton>
            </div>
            <div>
                <UDropdownMenu :items="items" :ui="{content: 'w-48'}">
                    <UButton icon="i-lucide-plus" color="primary" size="xl" variant="solid">
                        Ajoute carte
                    </UButton>
                </UDropdownMenu>
            </div>
        </div>
        <div class="card-account-list grid sm:grid-cols-2 md:grid-cols-3 gap-2">
            <CardResumeAnalytics 
                title="Revenu net du mois"
                :amount="incomeCardInfo.amount"
                :chip-info="incomeCardInfo.cardInfo"
                :is-percentage="false"
                :description="incomeCardInfo.description"
                :is-positive="incomeCardInfo.isPositif" />
            <CardResumeAnalytics 
                title="Depense totales"
                :amount="spendCardInfo.amount"
                :chip-info="spendCardInfo.cardInfo"
                :is-percentage="false"
                :description="spendCardInfo.description"
                :is-positive="spendCardInfo.isPositif" />
            <CardResumeAnalytics 
                title="Cashflow net"
                :amount="cashflowCardInfo.amount"
                :chip-info="cashflowCardInfo.cardInfo"
                :is-percentage="false"
                :description="cashflowCardInfo.description"
                :is-positive="cashflowCardInfo.isPositif" />
            <CardResumeAnalytics 
                title="Taux d'epargne"
                :amount="savingCardInfo.amount"
                :is-percentage="true"
                :chip-info="savingCardInfo.cardInfo"
                :description="savingCardInfo.description"
                :is-positive="savingCardInfo.isPositif" />
        </div> 
        <div class="card-grid-list sm:grid-cols-1 md:grid-cols-3 grid gap-2">

            <div class="card-grid rounded-md md:col-span-2">
                <CustomCardTitle title="Money flow" />
                <div class="flex justify-center items-center" style="height: 280px;">
                    <BarChart  :data="dataChart" :options="optionsChart" />
                </div>
            </div>
            <div class="card-grid rounded-md">
                <CustomCardTitle title="Budgets">
                    <UButton icon="i-lucide-external-link" variant="outline" color="neutral" />
                </CustomCardTitle>
                <div class="flex justify-center" style="height: 280px;">
                    <DoughnutChart :data="budgetChart" :options="optionsChart"/>
                </div>
            </div>
            <div class="card-grid rounded-md md:col-span-2 flex flex-col gap-2">
                <CustomCardTitle title="Depenses par categories">
                </CustomCardTitle>
                <div class="flex justify-center items-center" style="height: 280px;">
                    <BarChart  :data="dataSpendChart" :options="optionsChart" />
                </div>
            </div>

            <div class="card-grid rounded-md">
                <CustomCardTitle title="But d'epargne">
                    <UButton icon="i-lucide-external-link" variant="outline" color="neutral" />
                </CustomCardTitle>
                <div class="flex flex-col gap-1">
                    <div v-for="goal in displayGoals" :key="goal.id">
                        <BarBudgetInfo :id="goal.id" :title="goal.title" 
                        :target-amount="goal.target" :amount="goal.balance" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.option-diplay-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1rem;
}
.card-grid-list {
    margin-top: 1rem;
}
.card-grid {
    border: solid 1px #E6E6E6;
    padding: 0.5rem;
}
.card-account-list {
    // display: flex;
    // flex-direction: columns;
    margin-top: 1rem;
    // width: 100%;
    // overflow: auto;
    // flex-wrap: wrap;
}
</style>