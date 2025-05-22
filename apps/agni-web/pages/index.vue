<script setup lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { library } from '@fortawesome/fontawesome-svg-core'
import { computed, ref } from 'vue'
import { useFetchResumeAccount } from '../composables/account';
import { useListTransactions } from '../composables/transactions';
import { useListGoals } from '../composables/goals';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, type ChartData } from 'chart.js';
import { Bar } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const data = {
        labels: ['Jan', 'Feb', 'Marc'],
        datasets: [{data: [40, 20, 12]}]
}
const optionData = {
    responsive: true
}

library.add(fas)


const accounts = useFetchResumeAccount()
const transactionAccountSelected = ref(accounts.value[0].title)
const accountsChecked = ref(accounts.value.map(acc => ({id: acc.id, checked: true})))
const items = computed(() => accounts.value.map(acc => (
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
    }
)))
const transactions = useListTransactions(1, 4)
const goals = useListGoals()
const dateDisplayed = ref("Mois")
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
</script>

<template>
    <div>

        <div class="option-diplay-container">
            <div class="flex gap-2">
                <UDropdownMenu :items="listTypeDateDisplay">
                    <UButton size="xl" variant="outline" color="neutral">
                        <font-awesome-icon class="icon" icon="fa-solid fa-calendar-days" />
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
            <div  v-for="account in accounts.filter(e => accountsChecked.find(f => f.id == e.id && f.checked))" 
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
                <Bar :data="data" :options="optionData" /> 
            </div>
            <div class="card-grid rounded-md">
                <CustomCardTitle title="Budgets" />
            </div>

            <div class="card-grid rounded-md md:col-span-2 flex flex-col gap-2">
                <CustomCardTitle title="Transactions">
                    <div class="flex gap-1">
                        <USelect class="rounded-full" v-model="transactionAccountSelected" :items="accounts.map(acc => (acc.title))" />
                        <UButton class="rounded-full" size="sm" label="Voir plus" variant="outline" color="neutral" />
                    </div>
                </CustomCardTitle>
                <div>
                    <div class="flex flex-col gap-1" >
                        <div v-for="trans in transactions" :key="trans.id">
                            <RowTransaction 
                                :id="trans.id" :balance="trans.amount" :title="trans.title" 
                                :description="trans.description" :icon="trans.icon" 
                                :tags="trans.tags.map(tag=>tag.title)"/>    
                        </div>
                    </div>
                </div> 
            </div>

            <div class="card-grid rounded-md">
                <CustomCardTitle title="But d'epargne">
                    <UButton icon="i-lucide-external-link" variant="outline" color="neutral" />
                </CustomCardTitle>
                <div class="flex flex-col gap-1">
                    <div v-for="goal in goals" :key="goal.id">
                        <BarBudgetInfo :id="goal.id" :title="goal.title" 
                        :target-amount="goal.targetAmount" :amount="goal.amount" />
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