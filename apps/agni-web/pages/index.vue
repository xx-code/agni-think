<script setup lang="ts">
import useAccountsWitPastBalance, { ALL_ACCOUNT_ID } from '~/composables/accounts/useAccountsWithPastBalance'
import useBudgets from '~/composables/budgets/useBudgets'
import useSaveGoals from '~/composables/goals/useSaveGoals'
import useTransactionPagination from '~/composables/transactions/useTransactionPagination'
import type { FilterTransactionQuery } from '~/types/api/transaction'
import { formatBudgetDataForChart } from '~/utils/formatBudgetDataForChart'


const labelsDate = ["Jan", "Feb", "Mar", "Apr", "May", "Jun"]
const optionsChart = computed(() => ({responsive: true})) 
const dataChart = computed(() => ({
    labels: labelsDate, 
    datasets: [{
        label: 'Income',
        data: [65, 59, 80, 81, 56, 55],
        backgroundColor: '#6655d7',
        borderWidth: 1,
    }, {
        label: 'Spend',
        data: [60, 89, 45, 34, 60, 65],
        backgroundColor: 'rgba(103, 85, 215, 0.1)',
        borderWidth: 1
    }],
}))

const {data: budgets} = useBudgets()

const budgetChart = computed(() => {
    return formatBudgetDataForChart(budgets.value?.items) // TODO: review
});
// generate code
const now = new Date();
const prevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
const startDate = new Date(prevMonth.getFullYear(), prevMonth.getMonth(), 1)
const endDate = new Date(prevMonth.getFullYear(), prevMonth.getMonth() + 1, 0)

const {data:accounts, error:accountError, refresh:accountRefresh} = useAccountsWitPastBalance(startDate, endDate);

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
const paramsTransaction = reactive<FilterTransactionQuery>({offset: 0, limit: 4})
const {data: transactions} = useTransactionPagination(paramsTransaction);
const {data: goals} =  useSaveGoals();
const dateDisplayed = ref("Mois");
const listTypeDateDisplay =computed(() => (
[
    {
        label: 'Semaine' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            dateDisplayed.value = "Semain"
        } 
    },
    {
        label: 'Mois' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Mois"
        } 
    },
    {
        label: 'Trimestre' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Trimestre"
        } 
    },
    {
        label: 'Semestre' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Semestre"
        } 
    },
    {
        label: 'Annee' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
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
                <UDropdownMenu :items="listTypeDateDisplay"> <UButton size="xl" variant="outline" color="neutral">
                        <UIcon name="i-lucide-calendar"/>
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
                <CustomCardTitle title="Transactions">
                    <div class="flex gap-1">
                        <USelect class="rounded-full" v-model="transactionAccountSelected" :items="accounts?.items.map(acc => (acc.title))" />
                        <UButton class="rounded-full" size="sm" label="Voir plus" variant="outline" color="neutral" />
                    </div>
                </CustomCardTitle>
                <div>
                    <div class="flex flex-col gap-1" >
                        <div v-for="trans in transactions?.items" :key="trans.accountId">
                            <!-- <RowTransaction 
                                :id="trans.accountId" :balance="trans.amount" :title="trans.description" 
                                :description="trans.description" :icon="trans.category.icon" 
                                :tags="trans.tagRefs.map((i:any) => i.value)"/>     -->
                        </div>
                    </div>
                </div> 
            </div>

            <div class="card-grid rounded-md">
                <CustomCardTitle title="But d'epargne">
                    <UButton icon="i-lucide-external-link" variant="outline" color="neutral" />
                </CustomCardTitle>
                <div class="flex flex-col gap-1">
                    <div v-for="goal in goals?.items" :key="goal.id">
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