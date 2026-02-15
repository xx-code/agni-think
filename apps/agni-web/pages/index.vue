<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import useBudgets from '~/composables/budgets/useBudgets'
import useCategories from '~/composables/categories/useCategories'
import useSaveGoals from '~/composables/goals/useSaveGoals'
import useTransactionPagination from '~/composables/invoices/useTransactionPagination'
import { formatBudgetDataForChart } from '~/utils/formatBudgetDataForChart'
import { getListTime } from '~/utils/getListTime'
import { fetchAnalyticSavings } from '~/composables/analytics/useSaving'
import { fetchSpendByCategoriesAnalytic } from '~/composables/analytics/useSpends'
import { fetchBalanceByPeriod } from '~/composables/invoices/useBalance'
import type { QueryFilterRequest } from '~/types/api'
import type { GetSavingAnalysticRequest, GetSpendCategoryRequest } from '~/types/api/analytics'
import type { GetBalanceResponse, QueryBalanceByPeriod, QueryInvoice } from '~/types/api/transaction'
import type { SavingAnalysticType } from '~/types/ui/analytics'

// ─── Calendar ─────────────────────────────────────────────────────────────────
const calendarSelection = reactive<{
  period: 'Year' | 'Month' | 'Week'
  interval: number
  showNumber: number
  startDate: string
}>({
  period: 'Month',
  interval: 1,
  showNumber: 6,
  startDate: (() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 6)
    d.setDate(1)
    return d.toISOString()
  })()
})

watch(
  () => [calendarSelection.period, calendarSelection.interval, calendarSelection.showNumber],
  () => {
    const d = new Date()
    if (calendarSelection.period === 'Month')
    {
      d.setMonth(d.getMonth() - calendarSelection.showNumber * calendarSelection.interval)
      d.setDate(1)
    }
    else if (calendarSelection.period === 'Week')
      d.setDate(d.getDate() - calendarSelection.showNumber * calendarSelection.interval * 7)
    else if (calendarSelection.period === 'Year')
      d.setFullYear(d.getFullYear() - calendarSelection.showNumber * calendarSelection.interval)

    calendarSelection.startDate = d.toISOString()
  },
)

// ─── Period selector ──────────────────────────────────────────────────────────
const dateDisplayed = ref('Mois')
const periodOptions = [
  { label: 'Bi-hebdo', type: 'checkbox' as const, onSelect(e: Event) { e.preventDefault(); Object.assign(calendarSelection, { period: 'Week', interval: 2, showNumber: 6 }); dateDisplayed.value = 'Bi-hebdo' } },
  { label: 'Mois', type: 'checkbox' as const, onSelect(e: Event) { e.preventDefault(); Object.assign(calendarSelection, { period: 'Month', interval: 1, showNumber: 6 }); dateDisplayed.value = 'Mois' } },
  { label: 'Trimestre', type: 'checkbox' as const, onSelect(e: Event) { e.preventDefault(); Object.assign(calendarSelection, { period: 'Month', interval: 3, showNumber: 4 }); dateDisplayed.value = 'Trimestre' } },
  { label: 'Semestre', type: 'checkbox' as const, onSelect(e: Event) { e.preventDefault(); Object.assign(calendarSelection, { period: 'Month', interval: 6, showNumber: 4 }); dateDisplayed.value = 'Semestre' } },
  { label: 'Année', type: 'checkbox' as const, onSelect(e: Event) { e.preventDefault(); Object.assign(calendarSelection, { period: 'Year', interval: 1, showNumber: 3 }); dateDisplayed.value = 'Année' } },
]

// ─── Data fetching ────────────────────────────────────────────────────────────
const balanceData = ref<GetBalanceResponse[]>([])
const isLoading = ref(false)
const savingData = ref<SavingAnalysticType | null>(null)
const spendCategoryData = ref<{ categoryId: string; spends: number[] }[]>([])

async function fetchBalance() {
  isLoading.value = true
  try {
    balanceData.value = await fetchBalanceByPeriod({
      period: calendarSelection.period.toLowerCase(),
      interval: calendarSelection.interval,
      dateFrom: calendarSelection.startDate,
      isFreeze: false,
    } as QueryBalanceByPeriod)
  } catch (e) { balanceData.value = [] }
  finally { isLoading.value = false }
}

async function fetchSavings() {
  try {
    savingData.value = await fetchAnalyticSavings({
      period: calendarSelection.period.toLowerCase(),
      interval: calendarSelection.interval,
      startDate: calendarSelection.startDate,
    } as GetSavingAnalysticRequest)
  } catch (e) { savingData.value = null }
}

async function fetchSpendCategories() {
  try {
    const res = await fetchSpendByCategoriesAnalytic({
      period: calendarSelection.period.toLowerCase(),
      interval: calendarSelection.interval,
      startDate: calendarSelection.startDate,
      offset: 0,
      limit: 0,
      queryAll: true
    } as GetSpendCategoryRequest)

    spendCategoryData.value = res.items ?? []

  } catch (e) { spendCategoryData.value = [] }
}

watch(() => calendarSelection.startDate, () => { fetchBalance(); fetchSavings(); fetchSpendCategories() }, { immediate: true })

// ─── Supporting data ──────────────────────────────────────────────────────────
const { data: categories } = useCategories({ limit: 0, offset: 0, queryAll: true })
const { data: budgets } = useBudgets({ limit: 0, offset: 0, queryAll: true })
const { data: goals } = useSaveGoals({ limit: 6, offset: 0, queryAll: false })
const paramsTransaction = reactive<QueryFilterRequest & QueryInvoice>({ offset: 0, limit: 5, status: 'Pending' })
const { data: transactions } = useTransactionPagination(paramsTransaction)

const getCategory = (id: string) => categories.value?.items.find(i => i.id === id)
function pct(prev: number, curr: number) { return prev === 0 ? 0 : ((curr - prev) / Math.abs(prev)) * 100 }

// ─── Labels ───────────────────────────────────────────────────────────────────
const labels = computed(() => getListTime(calendarSelection.period, { count: calendarSelection.showNumber, spacing: calendarSelection.interval }))

// ─── KPIs ─────────────────────────────────────────────────────────────────────
const incomeKpi = computed(() => {
  const arr = balanceData.value.map(b => b.income)
  const current = arr.at(-1) ?? 0; const prev = arr.at(-2) ?? 0
  return { current, change: pct(prev, current), positive: current >= prev }
})
const spendKpi = computed(() => {
  const arr = balanceData.value.map(b => b.spend)
  const current = arr.at(-1) ?? 0; const prev = arr.at(-2) ?? 0
  return { current, change: pct(prev, current), positive: current <= prev }
})
const cashflowKpi = computed(() => {
  const arr = balanceData.value.map(b => b.balance)
  const current = arr.at(-1) ?? 0; const prev = arr.at(-2) ?? 0
  return { current, change: pct(prev, current), positive: current >= 0 }
})
const savingKpi = computed(() => {
  const rate = (savingData.value?.savingRates?.at(-1) ?? 0) * 100
  const amount = savingData.value?.savings?.at(-1) ?? 0
  return { rate, amount, positive: rate >= 10 }
})

// ─── Charts ───────────────────────────────────────────────────────────────────
const chartOpts = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
const donutOpts = { responsive: true, maintainAspectRatio: false, cutout: '70%', plugins: { legend: { display: false } } }

const cashflowChartData = computed(() => ({
  labels: labels.value,
  datasets: [
    { label: 'Revenus', data: balanceData.value.map(b => b.income), backgroundColor: '#10b981', borderRadius: 6, borderWidth: 0 },
    { label: 'Dépenses', data: balanceData.value.map(b => b.spend), backgroundColor: '#f43f5e', borderRadius: 6, borderWidth: 0 },
  ],
}))

const balanceLineData = computed(() => ({
  labels: labels.value,
  datasets: [{
    label: 'Solde net',
    data: balanceData.value.map(b => b.balance),
    borderColor: '#6366f1',
    backgroundColor: 'rgba(99,102,241,0.08)',
    fill: true, tension: 0.4, borderWidth: 2, pointRadius: 3,
  }],
}))

const categoryTotals = computed(() =>
  spendCategoryData.value
    .map(item => ({ categoryId: item.categoryId, total: item.spends.at(-1) ?? 0 }))
    .filter(i => i.total > 0).sort((a, b) => b.total - a.total).slice(0, 6),
)

const spendDonutData = computed(() => {
  const top = categoryTotals.value
  const cats = top.map(i => getCategory(i.categoryId))
  return {
    labels: cats.map(c => c?.title ?? '?'),
    datasets: [{ data: top.map(i => i.total), backgroundColor: cats.map(c => c?.color ?? '#6366f1'), borderWidth: 2, borderColor: '#ffffff', hoverOffset: 8 }],
  }
})

const topCategories = computed(() =>
  spendCategoryData.value
    .map(item => ({ categoryId: item.categoryId, spend: item.spends.at(-1) ?? 0 }))
    .filter(i => i.spend > 0).sort((a, b) => b.spend - a.spend).slice(0, 5),
)

const savingsChartData = computed(() => ({
  labels: labels.value,
  datasets: [
    { label: 'Épargne', data: savingData.value?.savings ?? [], backgroundColor: '#8b5cf6', borderRadius: 6, borderWidth: 0 },
    { label: 'Investissement', data: savingData.value?.investments ?? [], backgroundColor: '#ec4899', borderRadius: 6, borderWidth: 0 },
  ],
}))

const budgetChartData = computed(() => formatBudgetDataForChart(budgets.value?.items))

// ─── Modal ────────────────────────────────────────────────────────────────────
type ModalType = 'spend' | 'saving' | 'budget' | 'goals' | null
const modal = ref<ModalType>(null)
const modalTitles: Record<NonNullable<ModalType>, string> = {
  spend: 'Dépenses par catégories',
  saving: 'Épargne & Investissements',
  budget: 'Budgets',
  goals: "Objectifs d'épargne",
}
</script>

<template>
  <!-- Root: warm off-white, no dark -->
  <div class="min-h-screen bg-slate-50 text-slate-800">

    <div class="max-w-screen-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 pb-16">

      <!-- ── Header ── -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <p class="text-xs uppercase tracking-widest text-slate-400 font-medium mb-1">Vue d'ensemble</p>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900">Tableau de Bord</h1>
        </div>
        <UDropdownMenu :items="periodOptions">
          <button class="flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 bg-white text-sm font-medium text-slate-700 shadow-sm hover:border-slate-300 hover:bg-slate-50 transition-colors cursor-pointer">
            <UIcon name="i-lucide-calendar-days" class="text-slate-400" />
            {{ dateDisplayed }}
            <UIcon name="i-lucide-chevron-down" class="text-slate-400 text-xs" />
          </button>
        </UDropdownMenu>
      </div>

      <!-- ── Loading ── -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center gap-3 py-24 text-slate-400">
        <div class="w-8 h-8 rounded-full border-2 border-slate-200 border-t-indigo-500 animate-spin" />
        <span class="text-sm">Chargement…</span>
      </div>

      <template v-else>

        <!-- ── KPI Cards ── -->
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-5">

          <!-- Income — green tint -->
          <div class="flex items-center gap-4 p-5 rounded-2xl bg-emerald-50 border border-emerald-100 hover:shadow-md hover:shadow-emerald-100 transition-all duration-200">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-emerald-500 text-white flex-shrink-0 shadow-sm">
              <UIcon name="i-lucide-arrow-down-to-line" class="text-lg" />
            </div>
            <div class="min-w-0 flex flex-col gap-0.5">
              <span class="text-[10px] uppercase tracking-widest text-emerald-600 font-semibold">Revenus</span>
              <span class="text-xl font-bold tracking-tight text-slate-900 truncate">{{ formatCurrency(incomeKpi.current) }}</span>
              <span :class="['inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full w-fit', incomeKpi.positive ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700']">
                <UIcon :name="incomeKpi.positive ? 'i-lucide-trending-up' : 'i-lucide-trending-down'" />
                {{ incomeKpi.change > 0 ? '+' : '' }}{{ incomeKpi.change.toFixed(1) }}%
              </span>
            </div>
          </div>

          <!-- Spend — rose tint -->
          <div class="flex items-center gap-4 p-5 rounded-2xl bg-rose-50 border border-rose-100 hover:shadow-md hover:shadow-rose-100 transition-all duration-200">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-rose-500 text-white flex-shrink-0 shadow-sm">
              <UIcon name="i-lucide-arrow-up-from-line" class="text-lg" />
            </div>
            <div class="min-w-0 flex flex-col gap-0.5">
              <span class="text-[10px] uppercase tracking-widest text-rose-600 font-semibold">Dépenses</span>
              <span class="text-xl font-bold tracking-tight text-slate-900 truncate">{{ formatCurrency(spendKpi.current) }}</span>
              <span :class="['inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full w-fit', spendKpi.positive ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700']">
                <UIcon :name="spendKpi.positive ? 'i-lucide-trending-down' : 'i-lucide-trending-up'" />
                {{ spendKpi.change > 0 ? '+' : '' }}{{ spendKpi.change.toFixed(1) }}%
              </span>
            </div>
          </div>

          <!-- Cashflow — blue tint -->
          <div class="flex items-center gap-4 p-5 rounded-2xl bg-sky-50 border border-sky-100 hover:shadow-md hover:shadow-sky-100 transition-all duration-200">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-sky-500 text-white flex-shrink-0 shadow-sm">
              <UIcon name="i-lucide-arrow-right-left" class="text-lg" />
            </div>
            <div class="min-w-0 flex flex-col gap-0.5">
              <span class="text-[10px] uppercase tracking-widest text-sky-600 font-semibold">Solde net</span>
              <span :class="['text-xl font-bold tracking-tight truncate', cashflowKpi.positive ? 'text-emerald-600' : 'text-rose-600']">
                {{ formatCurrency(cashflowKpi.current) }}
              </span>
              <span :class="['inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full w-fit', cashflowKpi.positive ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700']">
                <UIcon :name="cashflowKpi.positive ? 'i-lucide-trending-up' : 'i-lucide-trending-down'" />
                {{ cashflowKpi.change > 0 ? '+' : '' }}{{ cashflowKpi.change.toFixed(1) }}%
              </span>
            </div>
          </div>

          <!-- Saving rate — violet tint -->
          <div class="flex items-center gap-4 p-5 rounded-2xl bg-violet-50 border border-violet-100 hover:shadow-md hover:shadow-violet-100 transition-all duration-200">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-violet-500 text-white flex-shrink-0 shadow-sm">
              <UIcon name="i-lucide-piggy-bank" class="text-lg" />
            </div>
            <div class="min-w-0 flex flex-col gap-0.5">
              <span class="text-[10px] uppercase tracking-widest text-violet-600 font-semibold">Taux d'épargne</span>
              <span :class="['text-xl font-bold tracking-tight', savingKpi.positive ? 'text-emerald-600' : 'text-rose-600']">
                {{ savingKpi.rate.toFixed(1) }}%
              </span>
              <span :class="['inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full w-fit', savingKpi.positive ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700']">
                <UIcon :name="savingKpi.positive ? 'i-lucide-check-circle-2' : 'i-lucide-alert-circle'" />
                {{ savingKpi.positive ? '≥ 10 % ✓' : '< 10 %' }}
              </span>
            </div>
          </div>
        </div>

        <!-- ── Charts Row 1 ── -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-4">

          <!-- Money Flow (2 cols) -->
          <div class="lg:col-span-2 rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden hover:shadow-md transition-shadow duration-200">
            <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Money Flow</h2>
              <div class="flex items-center gap-4 text-[11px] text-slate-400 font-medium">
                <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-emerald-500" />Revenus</span>
                <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-rose-500" />Dépenses</span>
              </div>
            </div>
            <div class="p-5 h-56">
              <BarChart :data="cashflowChartData" :options="chartOpts" />
            </div>
          </div>

          <!-- Net balance line -->
          <div class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden hover:shadow-md transition-shadow duration-200">
            <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Solde net</h2>
              <span class="flex items-center gap-1.5 text-[11px] text-slate-400 font-medium">
                <span class="w-2 h-2 rounded-full bg-indigo-500" />Solde
              </span>
            </div>
            <div class="p-5 h-56">
              <LineChart :data="balanceLineData" :options="chartOpts" />
            </div>
          </div>
        </div>

        <!-- ── Charts Row 2 ── -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-5">

          <!-- Savings (2 cols) -->
          <div
            class="lg:col-span-2 rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden cursor-pointer hover:shadow-md transition-shadow duration-200"
            @click="modal = 'saving'">
            <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Épargne & Investissements</h2>
              <div class="flex items-center gap-4 text-[11px] text-slate-400 font-medium">
                <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-violet-500" />Épargne</span>
                <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-pink-500" />Invest.</span>
              </div>
            </div>
            <div class="p-5 h-48">
              <BarChart :data="savingsChartData" :options="chartOpts" />
            </div>
          </div>

          <!-- Spend donut -->
          <div
            class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden cursor-pointer hover:shadow-md transition-shadow duration-200"
            @click="modal = 'spend'">
            <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Catégories</h2>
              <UIcon name="i-lucide-external-link" class="text-slate-400 text-sm" />
            </div>
            <div class="p-5">
              <div class="h-36">
                <DoughnutChart :data="spendDonutData" :options="donutOpts" />
              </div>
              <ul class="mt-3 flex flex-col gap-2">
                <li
                  v-for="item in categoryTotals.slice(0, 5)"
                  :key="item.categoryId"
                  class="flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: getCategory(item.categoryId)?.color ?? '#6366f1' }" />
                  <span class="flex-1 text-[11px] text-slate-500 truncate">{{ getCategory(item.categoryId)?.title ?? '—' }}</span>
                  <span class="text-[11px] font-semibold text-slate-700">{{ formatCurrency(item.total) }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- ── Bottom Row ── -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">

          <!-- Top-5 categories -->
          <div class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Top dépenses — période actuelle</h2>
            </div>
            <div class="p-5">
              <div v-if="topCategories.length === 0" class="flex flex-col items-center gap-2 py-8 text-slate-300 text-sm">
                <UIcon name="i-lucide-inbox" class="text-3xl" />
                <span>Aucune dépense</span>
              </div>
              <ul v-else class="flex flex-col gap-3.5">
                <li v-for="(item, i) in topCategories" :key="item.categoryId" class="flex items-center gap-2.5">
                  <span class="text-[10px] text-slate-300 w-3 text-right flex-shrink-0 font-medium">{{ i + 1 }}</span>
                  <UIcon
                    :name="getCategory(item.categoryId)?.icon ?? 'i-lucide-circle'"
                    class="flex-shrink-0 text-base"
                    :style="{ color: getCategory(item.categoryId)?.color ?? '#6366f1' }" />
                  <span class="w-20 flex-shrink-0 text-[11px] text-slate-500 truncate font-medium">
                    {{ getCategory(item.categoryId)?.title ?? '—' }}
                  </span>
                  <div class="flex-1 h-1.5 rounded-full bg-slate-100 overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all duration-500"
                      :style="{
                        width: topCategories[0].spend ? `${(item.spend / topCategories[0].spend) * 100}%` : '0%',
                        background: getCategory(item.categoryId)?.color ?? '#6366f1',
                      }" />
                  </div>
                  <span class="text-[11px] font-semibold flex-shrink-0 text-slate-700">{{ formatCurrency(item.spend) }}</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- Goals -->
          <div
            class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden cursor-pointer hover:shadow-md transition-shadow duration-200"
            @click="modal = 'goals'">
            <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Objectifs d'épargne</h2>
              <UIcon name="i-lucide-external-link" class="text-slate-400 text-sm" />
            </div>
            <div class="p-5">
              <div v-if="!goals?.items?.length" class="flex flex-col items-center gap-2 py-8 text-slate-300 text-sm">
                <UIcon name="i-lucide-target" class="text-3xl" />
                <span>Aucun objectif</span>
              </div>
              <ul v-else class="flex flex-col gap-4">
                <li v-for="goal in goals.items" :key="goal.id" class="flex flex-col gap-1.5">
                  <div class="flex items-baseline justify-between">
                    <span class="text-sm font-medium text-slate-800 truncate">{{ goal.title }}</span>
                    <span class="text-[11px] font-bold text-violet-500 flex-shrink-0 ml-2">
                      {{ ((goal.balance / goal.target) * 100).toFixed(0) }}%
                    </span>
                  </div>
                  <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
                    <div
                      class="h-full rounded-full bg-gradient-to-r from-violet-500 to-pink-500 transition-all duration-500"
                      :style="{ width: `${Math.min((goal.balance / goal.target) * 100, 100)}%` }" />
                  </div>
                  <div class="flex gap-1 text-[10px] text-slate-400 font-medium">
                    <span class="text-violet-600">{{ formatCurrency(goal.balance) }}</span>
                    <span>/ {{ formatCurrency(goal.target) }}</span>
                  </div>
                </li>
              </ul>
            </div>
          </div>

          <!-- Pending transactions -->
          <div class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
              <h2 class="text-sm font-semibold text-slate-800">Transactions en attente</h2>
            </div>
            <div class="p-5">
              <div v-if="!transactions?.items?.length" class="flex flex-col items-center gap-2 py-8 text-slate-300 text-sm">
                <UIcon name="i-lucide-clock" class="text-3xl" />
                <span>Aucune transaction</span>
              </div>
              <ul v-else class="flex flex-col gap-3">
                <li v-for="tx in transactions.items" :key="tx.id" class="flex items-center gap-3">
                  <div :class="['w-9 h-9 rounded-xl flex items-center justify-center text-sm flex-shrink-0 font-medium', tx.mouvement === 'INCOME' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600']">
                    <UIcon :name="tx.mouvement === 'INCOME' ? 'i-lucide-arrow-down-left' : 'i-lucide-arrow-up-right'" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-slate-800 truncate">
                      {{ (tx.transactions?.length > 0 && tx.transactions[0].description) ?? '—' }}
                    </p>
                    <p class="text-[10px] text-slate-400">{{ new Date(tx.date).toLocaleDateString('fr-FR') }}</p>
                  </div>
                  <span :class="['text-sm font-bold flex-shrink-0', tx.mouvement === 'INCOME' ? 'text-emerald-600' : 'text-rose-600']">
                    {{ tx.mouvement === 'INCOME' ? '+' : '-' }}{{ formatCurrency(tx.total) }}
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>

      </template>
    </div>

    <!-- ── Modal ── -->
    <UModal v-model="modal" :ui="{ wrapper: 'max-w-2xl' }">
      <div v-if="modal" class="bg-white rounded-2xl border border-slate-100 shadow-xl p-6">

        <div class="flex items-center justify-between mb-6">
          <h2 class="text-base font-bold text-slate-900">{{ modalTitles[modal] }}</h2>
          <button
            class="w-8 h-8 rounded-xl border border-slate-200 text-slate-400 flex items-center justify-center hover:bg-slate-50 transition-colors cursor-pointer"
            @click="modal = null">
            <UIcon name="i-lucide-x" class="text-sm" />
          </button>
        </div>

        <!-- Spend -->
        <div v-if="modal === 'spend'" class="flex flex-col gap-5">
          <div class="h-52">
            <DoughnutChart :data="spendDonutData" :options="{ ...donutOpts, plugins: { legend: { display: true, position: 'right', labels: { color: '#64748b', boxWidth: 10, font: { size: 11 } } } } }" />
          </div>
          <ul class="flex flex-col gap-2.5">
            <li v-for="(item, i) in topCategories" :key="item.categoryId" class="flex items-center gap-3">
              <span class="text-[10px] text-slate-300 w-3 text-right flex-shrink-0">{{ i + 1 }}</span>
              <span class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: getCategory(item.categoryId)?.color ?? '#6366f1' }" />
              <span class="flex-1 text-xs text-slate-500 truncate">{{ getCategory(item.categoryId)?.title ?? '—' }}</span>
              <div class="w-28 h-1.5 rounded-full bg-slate-100 overflow-hidden">
                <div class="h-full rounded-full transition-all duration-500"
                  :style="{ width: topCategories[0].spend ? `${(item.spend / topCategories[0].spend) * 100}%` : '0%', background: getCategory(item.categoryId)?.color ?? '#6366f1' }" />
              </div>
              <span class="text-sm font-semibold flex-shrink-0 text-slate-800">{{ formatCurrency(item.spend) }}</span>
            </li>
          </ul>
        </div>

        <!-- Saving -->
        <div v-if="modal === 'saving'" class="flex flex-col gap-5">
          <div class="h-52">
            <BarChart :data="savingsChartData" :options="{ ...chartOpts, plugins: { legend: { display: true, labels: { color: '#64748b', boxWidth: 10, font: { size: 11 } } } } }" />
          </div>
          <div class="flex flex-wrap gap-2">
            <div v-for="(rate, i) in (savingData?.savingRates ?? [])" :key="i"
              :class="['flex flex-col items-center px-3 py-2 rounded-xl border text-xs font-semibold', rate >= 0.1 ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-rose-50 border-rose-200 text-rose-700']">
              <span class="text-[10px] font-normal text-slate-400 mb-0.5">{{ labels[i] }}</span>
              <span>{{ (rate * 100).toFixed(1) }}%</span>
            </div>
          </div>
        </div>

        <!-- Budget -->
        <div v-if="modal === 'budget'" class="flex flex-col gap-5">
          <div class="h-52">
            <DoughnutChart :data="budgetChartData" :options="{ ...donutOpts, plugins: { legend: { display: true, position: 'right', labels: { color: '#64748b', boxWidth: 10, font: { size: 11 } } } } }" />
          </div>
          <ul class="flex flex-col gap-3">
            <li v-for="b in (budgets?.items ?? [])" :key="b.id" class="flex flex-col gap-1.5">
              <div class="flex justify-between text-sm">
                <span class="font-medium text-slate-800">{{ b.title }}</span>
                <span class="text-xs text-slate-400">{{ formatCurrency(b.currentBalance) }} / {{ formatCurrency(b.target) }}</span>
              </div>
              <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
                <div class="h-full rounded-full bg-gradient-to-r from-sky-500 to-indigo-500 transition-all duration-500"
                  :style="{ width: `${Math.min((b.currentBalance / b.target) * 100, 100)}%` }" />
              </div>
            </li>
          </ul>
        </div>

        <!-- Goals -->
        <div v-if="modal === 'goals'">
          <ul class="flex flex-col gap-4">
            <li v-for="goal in (goals?.items ?? [])" :key="goal.id"
              class="p-4 rounded-xl bg-slate-50 border border-slate-100 flex flex-col gap-2">
              <div class="flex justify-between items-baseline">
                <span class="font-semibold text-slate-900">{{ goal.title }}</span>
                <span class="text-sm font-bold text-violet-500">{{ ((goal.balance / goal.target) * 100).toFixed(1) }}%</span>
              </div>
              <div class="h-2 rounded-full bg-slate-200 overflow-hidden">
                <div class="h-full rounded-full bg-gradient-to-r from-violet-500 to-pink-500 transition-all duration-500"
                  :style="{ width: `${Math.min((goal.balance / goal.target) * 100, 100)}%` }" />
              </div>
              <div class="flex gap-1.5 text-xs">
                <span class="font-semibold text-violet-600">{{ formatCurrency(goal.balance) }}</span>
                <span class="text-slate-400">/ {{ formatCurrency(goal.target) }}</span>
              </div>
            </li>
          </ul>
        </div>

      </div>
    </UModal>

  </div>
</template>