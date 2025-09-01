<script setup lang="ts">
import useAccountsWitPastBalance, { ALL_ACCOUNT_ID } from '~/composables/accounts/useAccountsWithPastBalance'
import useCashflow from '~/composables/analytics/useCashflow'
import useBudgets from '~/composables/budgets/useBudgets'
import useCategories from '~/composables/categories/useCategories'
import useSaveGoals from '~/composables/goals/useSaveGoals'
import useTags from '~/composables/tags/useTags'
import useTransactionPagination from '~/composables/transactions/useTransactionPagination'
import type { FilterTransactionQuery } from '~/types/api/transaction'
import type { TransactionTableType } from '~/types/ui/transaction'
import { formatBudgetDataForChart } from '~/utils/formatBudgetDataForChart'
import { getListTime } from '~/utils/getListTime'

const calendarSelection = reactive<{
    period: 'Year' | 'Month' | 'Week',
    periodTime: number,
    showNumber: number
}>({
    period: 'Month',
    periodTime: 1,
    showNumber: 6  
})

const { data: cashflow } = useCashflow(calendarSelection) 
const {data: categories, error: errorCategory, refresh: refreshCategory } = useCategories({
    limit: 0, offset: 0, queryAll: true
})
const {data: tags, error: errorTag, refresh: refreshTag } = useTags({
    limit: 0, offset: 0, queryAll: true
})

const displaytransactionsTable = computed(() => {
    const getCategory = (id: string) => categories.value?.items.find(i => id === i.id)
    const getTag = (id: string) => tags.value?.items.find(i => id === i.id)
    const getBudget = (id: string) => budgets.value?.items.find(i => id === i.id)

    return transactions.value?.items.map(i => ({
        id: i.id,
        accountId: i.accountId,
        amount: i.amount,
        date: i.date,
        description: i.description,
        recordType: i.recordType,
        type: i.type,
        status: i.status,
        category: {
            id: i.categoryId,
            icon: getCategory(i.categoryId)?.icon || '',
            color: getCategory(i.categoryId)?.color || '',
            title: getCategory(i.categoryId)?.title || '',
        },
        tags: i.tagIds.map(i => ({
            id: i,
            value: getTag(i)?.value || '',
            color: getTag(i)?.color || ''
        })),
        budgets: i.budgetIds.map(i => ({
            id: i,
            title: getBudget(i)?.title || ''
        }))
    } satisfies TransactionTableType))    
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

const {data: budgets} = useBudgets({
    limit: 0, offset: 0, queryAll: true
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
const {data: goals} =  useSaveGoals({limit: 4, offset: 0, sortSense: 'desc', orderBy: 'balance'});
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
                    <UButton icon="i-lucide-plus" size="xl" variant="solid">
                        Ajoute carte
                    </UButton>
                </UDropdownMenu>
            </div>
        </div>
        <div class="card-account-list grid sm:grid-cols-2 md:grid-cols-3 gap-2">
            <div  v-for="account in accounts?.items.filter(e => accountsChecked.find(f => f.id == e.id && f.checked))" 
                :key="account.id">
                <CardResumeAccount 
                    :id="account.id"
                    :title="account.title" 
                    :balance="account.balance"
                    :diff-past-balance-per="account.pastBalanceDetail.diffPercent"
                    :is-positif="account.pastBalanceDetail.doIncrease"
                    :allow-open=true
                    :past-date-info="'mois'"
                />
            </div>
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
                <CustomCardTitle title="Transactions en attende">
                </CustomCardTitle>
                <div>
                    <div class="flex flex-col gap-1" >
                        <div v-for="trans in displaytransactionsTable" :key="trans.accountId">
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
                    </div>
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