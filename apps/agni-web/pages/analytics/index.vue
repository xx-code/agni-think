<script setup lang="ts">
import { ref, reactive, computed, watch, watchEffect } from 'vue'
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder'
import { API_ROUTES } from '~/shared/routes'
import { getSpendingPeriodAnalyticRange } from '~/composables/spendingPeriod'
import { listCategoriesResponseToListCategories } from '~/mappers/category'
import { profileResponseToProfile } from '~/mappers/profile'
import { listSpendingPeriodsResponseToListSpendingPeriods } from '~/mappers/spending-period'
import type { ListResponse } from '~/types/api'
import type { GetAnnualOutlookResponse, GetBudgetingRuleRequest, GetBudgetingRuleResponse } from '~/types/api/analytics'
import type { GetCategoryResponse } from '~/types/api/category'
import type { GetProfileResponse } from '~/types/api/profile'
import type { GetAllSpendingPeriodResponse } from '~/types/api/spending-period'
import type { AllSpendingPeriod } from '~/types/ui/spending-period'
import { getLabelSpendingPeriodType, SpendingPeriodType } from '~/types/constants/spendingPeriod'
import { periodOptions } from '~/utils/constant'

const DAY_IN_MS = 86400000

// Single tenant setup, same profile id as the settings page
const PROFILE_ID = '457ae73e-8124-4d3b-ab2b-d6a404c6b4d3'

// ─── Filter Mode ──────────────────────────────────────────────────────────────
type FilterMode = 'period' | 'range' | 'spendingPeriod'

const filterMode = ref<FilterMode>('period')

const filterModeItems = computed(() => ([
  { label: 'Période', value: 'period' },
  { label: 'Dates', value: 'range' },
  { label: 'Période de dépenses', value: 'spendingPeriod' }
] satisfies { label: string, value: FilterMode }[]).map(item => ({
  label: item.label,
  type: 'checkbox' as const,
  checked: filterMode.value === item.value,
  onSelect(e: Event) {
    e.preventDefault()
    filterMode.value = item.value
  }
})))

const filterModeLabel = computed(() =>
  filterModeItems.value.find(i => i.checked)?.label ?? 'Période'
)

// ─── Period Filter ────────────────────────────────────────────────────────────
const periodFilter = reactive<{ period: string; interval: number }>({
  period: 'Month',
  interval: 1,
})

const selectedPeriodLabel = computed(() =>
  periodOptions.find(o => o.period === periodFilter.period && o.interval === periodFilter.interval)?.label ?? 'Mois'
)

// ─── Range Filter ─────────────────────────────────────────────────────────────
const rangeFilter = reactive({ startDate: '', endDate: '' })

const rangeFilterError = computed(() => {
  if (!rangeFilter.startDate || !rangeFilter.endDate)
    return 'Sélectionnez une date de début et de fin'

  if (rangeFilter.endDate < rangeFilter.startDate)
    return 'La date de fin doit être postérieure à la date de début'

  return undefined
})

// ─── Spending Period Filter ───────────────────────────────────────────────────
const spendingPeriodId = ref<string | undefined>(undefined)

const { data: spendingPeriods } = await useAsyncData('analytics+spending-periods', async () => {
  const res = await ApiLinkBuilder
    .route<ListResponse<GetAllSpendingPeriodResponse>>(API_ROUTES.SPENDING_PERIOD.GET_ALL_SPENDING_PERIOD)
    .query({
      'queryFilter.offset': 0,
      'queryFilter.limit': 0,
      'queryFilter.queryAll': true,
      'queryFilter.sortBy.by': 'start_date',
      'queryFilter.sortBy.ascending': false
    })
    .mapper(listSpendingPeriodsResponseToListSpendingPeriods)
    .execute()

  return res.items
})

// A draft has no real spending behind it, so it cannot produce a meaningful rule
const selectableSpendingPeriods = computed<AllSpendingPeriod[]>(() =>
  (spendingPeriods.value ?? []).filter(i => i.state !== SpendingPeriodType.Draft)
)

const spendingPeriodItems = computed(() =>
  selectableSpendingPeriods.value.map(period => ({
    label: `${formatDate(period.startDate)} → ${formatDate(period.endDate)} · ${getLabelSpendingPeriodType(period.state)}`,
    value: period.id
  }))
)

const selectedSpendingPeriod = computed(() =>
  selectableSpendingPeriods.value.find(i => i.id === spendingPeriodId.value)
)

// Default to the most recent usable period
watchEffect(() => {
  if (selectedSpendingPeriod.value)
    return

  spendingPeriodId.value = selectableSpendingPeriods.value[0]?.id
})

// ─── Target rule from the profile (read only) ────────────────────────────────
// The target rule is owned by the settings page, analytics only displays it
const { data: profile } = await useAsyncData('analytics+profile', async () => {
  return await ApiLinkBuilder
    .route<GetProfileResponse>(API_ROUTES.PROFILE.GET_PROFILE)
    .params({ id: PROFILE_ID })
    .mapper(profileResponseToProfile)
    .execute()
})

const ruleConfig = computed(() => ({
  needs: profile.value?.fixSpendPercentage ?? 0,
  wants: profile.value?.varialSpendPercentage ?? 0,
  savings: profile.value?.savingPercentage ?? 0
}))

// ─── Data ─────────────────────────────────────────────────────────────────────
const outlookData  = ref<GetAnnualOutlookResponse | null>(null)
const ruleData     = ref<GetBudgetingRuleResponse | null>(null)
const isLoadingOut = ref(false)
const isLoadingRule = ref(false)

const { data: categories } = useAsyncData('cats+outlook', () =>
  ApiLinkBuilder.route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES).query({ limit: 0, offset: 0, queryAll: true }).mapper(listCategoriesResponseToListCategories).execute().then(r => r.items)
)

const getCategory = (id: string) => categories.value?.find(c => c.id === id)

async function loadOutlook() {
    isLoadingOut.value = true
    try { 
        outlookData.value = await ApiLinkBuilder.route<GetAnnualOutlookResponse>(API_ROUTES.ANALYTICS.ANNUAL_OUTLOOK).execute()
    }
    catch { 
        outlookData.value = null 
    }
    finally { 
        isLoadingOut.value = false 
    }
}

// Analytics endpoints only read startDate + period + interval, so a custom range
// is sent as a day interval starting on the first day of the range
function toIsoStartOfDay(value: string) {
  if (!value)
    return undefined

  const date = new Date(`${value}T00:00:00`)

  return Number.isNaN(date.getTime()) ? undefined : date.toISOString()
}

const ruleQuery = computed<GetBudgetingRuleRequest | null>(() => {
  if (filterMode.value === 'period')
    return { period: periodFilter.period, interval: periodFilter.interval }

  if (filterMode.value === 'range') {
    if (rangeFilterError.value)
      return null

    const startDate = toIsoStartOfDay(rangeFilter.startDate)
    const endDate = toIsoStartOfDay(rangeFilter.endDate)

    if (!startDate || !endDate)
      return null

    const days = Math.max(1, Math.round((new Date(endDate).getTime() - new Date(startDate).getTime()) / DAY_IN_MS) + 1)

    return { startDate, period: 'Day', interval: days }
  }

  if (!selectedSpendingPeriod.value)
    return null

  const range = getSpendingPeriodAnalyticRange(selectedSpendingPeriod.value)

  return { startDate: range.startDate, period: range.period, interval: range.interval }
})

const ruleFilterLabel = computed(() => {
  if (filterMode.value === 'period')
    return selectedPeriodLabel.value

  if (filterMode.value === 'range')
    return `${rangeFilter.startDate} → ${rangeFilter.endDate}`

  return selectedSpendingPeriod.value
    ? `${formatDate(selectedSpendingPeriod.value.startDate)} → ${formatDate(selectedSpendingPeriod.value.endDate)}`
    : '—'
})

async function loadRule() {
    const query = ruleQuery.value

    if (!query) {
      ruleData.value = null
      return
    }

    isLoadingRule.value = true
    try { 
        ruleData.value = await ApiLinkBuilder.route<GetBudgetingRuleResponse>(API_ROUTES.ANALYTICS.BUDGETING_RULE).query(query).execute()
    }
    catch { 
        ruleData.value = null 
    }
    finally { 
        isLoadingRule.value = false 
    }
}

watch(ruleQuery, loadRule, { immediate: true })
onMounted(loadOutlook)

// ─── Outlook computed ─────────────────────────────────────────────────────────
const outlookProgress = computed(() => {
  if (!outlookData.value) return { income: 0, spend: 0, budget: 0, saving: 0 }
  const o = outlookData.value
  return {
    income: o.incomeOutlook > 0 ? Math.min((o.currentIncomeOutlook / o.incomeOutlook) * 100, 100) : 0,
    spend:  o.spendOutlook  > 0 ? Math.min((o.currentSpendOutlook  / o.spendOutlook)  * 100, 100) : 0,
    budget: o.budgetOutlook > 0 ? Math.min((o.currentBudgetOutlook / o.budgetOutlook) * 100, 100) : 0,
    saving: o.savingMargin  > 0 ? Math.min((o.currentSaving        / o.savingMargin)  * 100, 100) : 0,
  }
})

const topOutlookCategories = computed(() => {
  if (!outlookData.value) return []
  const current = Object.fromEntries(outlookData.value.currentSpendByCategoryOutlook.map(c => [c.categoryId, c.spend]))
  return [...outlookData.value.spendByCategoriesOutlook]
    .sort((a, b) => b.spend - a.spend)
    .slice(0, 6)
    .map(c => ({ ...c, current: current[c.categoryId] ?? 0, pct: c.spend > 0 ? ((current[c.categoryId] ?? 0) / c.spend) * 100 : 0 }))
})

// ─── Rule computed ────────────────────────────────────────────────────────────
const ruleSegments = computed(() => {
  if (!ruleData.value) return []
  const d = ruleData.value
  const target = ruleConfig.value

  // How far the actual ratio went compared to the target, as a percentage
  const targetPct = (ratio: number, goal: number) => goal > 0 ? (ratio / goal) * 100 : 0

  return [
    {
      label: 'Charges fixes',
      ratio: d.ratioFixCost,
      amount: d.fixCost,
      target: target.needs / 100,
      targetPct: targetPct(d.ratioFixCost, target.needs / 100),
      color: '#6366f1',
      bg: 'bg-indigo-500',
      light: 'bg-indigo-50',
      border: 'border-indigo-200',
      text: 'text-indigo-700',
      icon: 'i-lucide-home',
    },
    {
      label: 'Charge variable',
      ratio: d.ratioVariableCost,
      amount: d.variableCost,
      target: target.wants / 100,
      targetPct: targetPct(d.ratioVariableCost, target.wants / 100),
      color: '#f59e0b',
      bg: 'bg-amber-500',
      light: 'bg-amber-50',
      border: 'border-amber-200',
      text: 'text-amber-700',
      icon: 'i-lucide-shopping-bag',
    },
    {
      label: 'Épargne',
      ratio: d.ratioSaving,
      amount: d.savingAmount,
      target: target.savings / 100,
      targetPct: targetPct(d.ratioSaving, target.savings / 100),
      color: '#10b981',
      bg: 'bg-emerald-500',
      light: 'bg-emerald-50',
      border: 'border-emerald-200',
      text: 'text-emerald-700',
      icon: 'i-lucide-piggy-bank',
    },
  ]
})

const ruleBarData = computed(() => {
  if (!ruleData.value) return { labels: [], datasets: [] }
  return {
    labels: ['Charges fixes', 'Loisirs & envies', 'Épargne'],
    datasets: [
      {
        label: 'Réel',
        data: [ruleData.value.ratioFixCost * 100, ruleData.value.ratioVariableCost * 100, ruleData.value.ratioSaving * 100],
        backgroundColor: ['rgba(99,102,241,0.85)', 'rgba(245,158,11,0.85)', 'rgba(16,185,129,0.85)'],
        borderRadius: 8, borderWidth: 0,
      },
      {
        label: 'Cible',
        data: [ruleConfig.value.needs, ruleConfig.value.wants, ruleConfig.value.savings],
        backgroundColor: ['rgba(99,102,241,0.18)', 'rgba(245,158,11,0.18)', 'rgba(16,185,129,0.18)'],
        borderRadius: 8, borderWidth: 0,
      },
    ],
  }
})

const chartOpts = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: true, labels: { color: '#94a3b8', boxWidth: 10, font: { size: 11 } } } },
  scales: {
    x: { grid: { display: false }, ticks: { color: '#94a3b8', font: { size: 11 } } },
    y: { grid: { color: 'rgba(148,163,184,0.1)' }, ticks: { color: '#94a3b8', font: { size: 11 }, callback: (v: number) => v + '%' } },
  },
}

// ─── Donut chart for rule ─────────────────────────────────────────────────────
const ruleDonutData = computed(() => ({
  labels: ['Charges fixes', 'Loisirs', 'Épargne'],
  datasets: [{
    data: ruleData.value
      ? [ruleData.value.ratioFixCost * 100, ruleData.value.ratioVariableCost * 100, ruleData.value.ratioSaving * 100]
      : [0, 0, 0],
    backgroundColor: ['rgba(99,102,241,0.85)', 'rgba(245,158,11,0.85)', 'rgba(16,185,129,0.85)'],
    borderWidth: 2, borderColor: '#ffffff', hoverOffset: 6,
  }],
}))
const donutOpts = { responsive: true, maintainAspectRatio: false, cutout: '72%', plugins: { legend: { display: false } } }

// ─── Outlook donut ────────────────────────────────────────────────────────────
const outlookCategoryDonut = computed(() => {
  const items = topOutlookCategories.value
  return {
    labels: items.map(i => getCategory(i.categoryId)?.title ?? '?'),
    datasets: [{
      data: items.map(i => i.current),
      backgroundColor: items.map(i => getCategory(i.categoryId)?.color ?? '#6366f1'),
      borderWidth: 2, borderColor: '#ffffff', hoverOffset: 6,
    }],
  }
})

// ─── Helpers ──────────────────────────────────────────────────────────────────
function savingStatusColor(pct: number) {
  if (pct >= 90) return 'text-emerald-600'
  if (pct >= 60) return 'text-amber-500'
  return 'text-rose-500'
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <div class="max-w-screen-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 pb-20">

      <!-- ── Page Header ── -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <p class="text-xs uppercase tracking-widest text-slate-400 font-medium mb-1">Analyse financière</p>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900">Prévisions & Règle budgétaire</h1>
        </div>
      </div>

      <!-- ══════════════════════════════════════════════════════════════
           SECTION 1 — ANNUAL OUTLOOK
      ══════════════════════════════════════════════════════════════ -->
      <div class="mb-2">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-7 h-7 rounded-lg bg-indigo-500 flex items-center justify-center">
            <UIcon name="i-lucide-telescope" class="text-white text-sm" />
          </div>
          <h2 class="text-base font-bold text-slate-900">Prévisions annuelles</h2>
          <span class="text-[11px] text-slate-400 font-medium ml-1">Objectifs vs réalisé</span>
        </div>
      </div>

      <div v-if="isLoadingOut" class="flex items-center justify-center gap-3 py-20 text-slate-400">
        <div class="w-7 h-7 rounded-full border-2 border-slate-200 border-t-indigo-500 animate-spin" />
        <span class="text-sm">Chargement des prévisions…</span>
      </div>

      <template v-else>
        <!-- KPI Cards -->
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-5">

          <!-- Income outlook -->
          <div class="p-5 rounded-2xl bg-white border border-slate-100 shadow-sm hover:shadow-md hover:shadow-emerald-50 transition-all duration-200">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl bg-emerald-500 text-white flex items-center justify-center shadow-sm">
                  <UIcon name="i-lucide-trending-up" class="text-base" />
                </div>
                <span class="text-[11px] uppercase tracking-wider font-semibold text-emerald-600">Revenus</span>
              </div>
              <span class="text-[11px] font-bold text-slate-400">{{ outlookProgress.income.toFixed(0) }}%</span>
            </div>
            <div class="space-y-1 mb-3">
              <div class="text-xl font-bold text-slate-900 tracking-tight">{{ formatCurrency(outlookData?.currentIncomeOutlook ?? 0) }}</div>
              <div class="text-[11px] text-slate-400">/ {{ formatCurrency(outlookData?.incomeOutlook ?? 0) }} prévu</div>
            </div>
            <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div class="h-full rounded-full bg-emerald-500 transition-all duration-700"
                :style="{ width: outlookProgress.income + '%' }" />
            </div>
          </div>

          <!-- Spend outlook -->
          <div class="p-5 rounded-2xl bg-white border border-slate-100 shadow-sm hover:shadow-md hover:shadow-rose-50 transition-all duration-200">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl bg-rose-500 text-white flex items-center justify-center shadow-sm">
                  <UIcon name="i-lucide-receipt" class="text-base" />
                </div>
                <span class="text-[11px] uppercase tracking-wider font-semibold text-rose-600">Dépenses</span>
              </div>
              <span :class="['text-[11px] font-bold', outlookProgress.spend > 80 ? 'text-rose-500' : 'text-slate-400']">
                {{ outlookProgress.spend.toFixed(0) }}%
              </span>
            </div>
            <div class="space-y-1 mb-3">
              <div class="text-xl font-bold text-slate-900 tracking-tight">{{ formatCurrency(outlookData?.currentSpendOutlook ?? 0) }}</div>
              <div class="text-[11px] text-slate-400">/ {{ formatCurrency(outlookData?.spendOutlook ?? 0) }} prévu</div>
            </div>
            <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div class="h-full rounded-full transition-all duration-700"
                :class="outlookProgress.spend > 90 ? 'bg-rose-500' : outlookProgress.spend > 70 ? 'bg-amber-400' : 'bg-rose-400'"
                :style="{ width: outlookProgress.spend + '%' }" />
            </div>
          </div>

          <!-- Budget outlook -->
          <div class="p-5 rounded-2xl bg-white border border-slate-100 shadow-sm hover:shadow-md hover:shadow-sky-50 transition-all duration-200">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl bg-sky-500 text-white flex items-center justify-center shadow-sm">
                  <UIcon name="i-lucide-wallet" class="text-base" />
                </div>
                <span class="text-[11px] uppercase tracking-wider font-semibold text-sky-600">Budget</span>
              </div>
              <span class="text-[11px] font-bold text-slate-400">{{ outlookProgress.budget.toFixed(0) }}%</span>
            </div>
            <div class="space-y-1 mb-3">
              <div class="text-xl font-bold text-slate-900 tracking-tight">{{ formatCurrency(outlookData?.currentBudgetOutlook ?? 0) }}</div>
              <div class="text-[11px] text-slate-400">/ {{ formatCurrency(outlookData?.budgetOutlook ?? 0) }} prévu</div>
            </div>
            <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div class="h-full rounded-full bg-sky-500 transition-all duration-700"
                :style="{ width: outlookProgress.budget + '%' }" />
            </div>
          </div>

          <!-- Saving margin -->
          <div class="p-5 rounded-2xl bg-white border border-slate-100 shadow-sm hover:shadow-md hover:shadow-violet-50 transition-all duration-200">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl bg-violet-500 text-white flex items-center justify-center shadow-sm">
                  <UIcon name="i-lucide-piggy-bank" class="text-base" />
                </div>
                <span class="text-[11px] uppercase tracking-wider font-semibold text-violet-600">Épargne</span>
              </div>
              <span :class="['text-[11px] font-bold', savingStatusColor(outlookProgress.saving)]">
                {{ outlookProgress.saving.toFixed(0) }}%
              </span>
            </div>
            <div class="space-y-1 mb-3">
              <div :class="['text-xl font-bold tracking-tight', outlookData && outlookData.currentSaving < 0 ? 'text-rose-600' : 'text-slate-900']">
                {{ formatCurrency(outlookData?.currentSaving ?? 0) }}
              </div>
              <div class="text-[11px] text-slate-400">/ {{ formatCurrency(outlookData?.savingMargin ?? 0) }} marge</div>
            </div>
            <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div class="h-full rounded-full bg-gradient-to-r from-violet-500 to-pink-500 transition-all duration-700"
                :style="{ width: Math.max(0, outlookProgress.saving) + '%' }" />
            </div>
          </div>
        </div>

        <!-- Category outlook breakdown -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-8">

          <!-- Donut -->
          <div class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
              <h3 class="text-sm font-semibold text-slate-800">Répartition actuelle</h3>
            </div>
            <div class="p-5">
              <div class="h-44">
                <DoughnutChart :data="outlookCategoryDonut" :options="donutOpts" />
              </div>
            </div>
          </div>

          <!-- Category progress list -->
          <div class="lg:col-span-2 rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
              <h3 class="text-sm font-semibold text-slate-800">Prévisions par catégorie</h3>
            </div>
            <div class="p-5">
              <div v-if="topOutlookCategories.length === 0" class="flex flex-col items-center gap-2 py-8 text-slate-300 text-sm">
                <UIcon name="i-lucide-inbox" class="text-3xl" />
                <span>Aucune donnée</span>
              </div>
              <ul v-else class="flex flex-col gap-4">
                <li v-for="item in topOutlookCategories" :key="item.categoryId" class="grid grid-cols-[1.5rem_7rem_1fr_5rem_5rem] items-center gap-3">
                  <UIcon
                    :name="getCategory(item.categoryId)?.icon ?? 'i-lucide-circle'"
                    class="text-base flex-shrink-0"
                    :style="{ color: getCategory(item.categoryId)?.color ?? '#6366f1' }" />
                  <span class="text-xs font-medium text-slate-700 truncate">{{ getCategory(item.categoryId)?.title ?? '—' }}</span>
                  <div class="flex flex-col gap-1">
                    <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
                      <div class="h-full rounded-full transition-all duration-700"
                        :style="{ width: Math.min(item.pct, 100) + '%', background: getCategory(item.categoryId)?.color ?? '#6366f1' }" />
                    </div>
                    <div class="flex justify-between text-[10px] text-slate-400">
                      <span>{{ formatCurrency(item.current) }}</span>
                      <span>{{ formatCurrency(item.spend) }}</span>
                    </div>
                  </div>
                  <span class="text-xs font-semibold text-slate-600 text-right">{{ item.pct.toFixed(0) }}%</span>
                  <span :class="['text-[10px] font-semibold text-right', item.pct > 90 ? 'text-rose-500' : item.pct > 70 ? 'text-amber-500' : 'text-emerald-600']">
                    {{ item.pct > 100 ? '⚠ Dépassé' : item.pct > 90 ? 'Attention' : 'OK' }}
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </template>

      <!-- ══════════════════════════════════════════════════════════════
           SECTION 2 — BUDGETING RULE
      ══════════════════════════════════════════════════════════════ -->
      <div class="mb-4">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div class="flex items-center gap-3">
            <div class="w-7 h-7 rounded-lg bg-amber-500 flex items-center justify-center">
              <UIcon name="i-lucide-pie-chart" class="text-white text-sm" />
            </div>
            <h2 class="text-base font-bold text-slate-900">Règle budgétaire</h2>
            <span class="text-[11px] text-slate-400 font-medium ml-1">— {{ ruleConfig.needs }}/{{ ruleConfig.wants }}/{{ ruleConfig.savings }}</span>
          </div>

          <!-- Filter toggle -->
          <UDropdownMenu :items="filterModeItems">
            <button class="flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 bg-white text-sm font-medium text-slate-700 shadow-sm hover:border-slate-300 hover:bg-slate-50 transition-colors cursor-pointer">
              <UIcon name="i-lucide-calendar-days" class="text-slate-400" />
              {{ filterModeLabel }}
              <UIcon name="i-lucide-chevron-down" class="text-slate-400 text-xs" />
            </button>
          </UDropdownMenu>
        </div>
      </div>

      <!-- Filter bar -->
      <div class="bg-white border border-slate-100 rounded-2xl px-5 py-4 shadow-sm mb-5">
        <!-- Period mode -->
        <div v-if="filterMode === 'period'" class="flex flex-wrap items-center gap-3">
          <span class="text-xs font-semibold text-slate-500 mr-1">Période :</span>
          <button
            v-for="opt in periodOptions"
            :key="opt.label"
            :class="[
              'px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all duration-150',
              periodFilter.period === opt.period && periodFilter.interval === opt.interval
                ? 'bg-indigo-500 text-white border-indigo-500 shadow-sm'
                : 'text-slate-500 border-slate-200 hover:border-indigo-300 hover:text-indigo-600'
            ]"
            @click="Object.assign(periodFilter, { period: opt.period, interval: opt.interval })">
            {{ opt.label }}
          </button>
        </div>

        <!-- Range mode -->
        <div v-else-if="filterMode === 'range'" class="flex flex-col gap-2">
          <div class="flex flex-wrap items-center gap-4">
            <span class="text-xs font-semibold text-slate-500">Du :</span>
            <input
              v-model="rangeFilter.startDate"
              type="date"
              class="px-3 py-1.5 text-xs rounded-xl border border-slate-200 bg-slate-50 text-slate-700 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-200 transition" />
            <span class="text-xs font-semibold text-slate-500">au :</span>
            <input
              v-model="rangeFilter.endDate"
              :min="rangeFilter.startDate || undefined"
              type="date"
              class="px-3 py-1.5 text-xs rounded-xl border border-slate-200 bg-slate-50 text-slate-700 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-200 transition" />
          </div>
          <span v-if="rangeFilterError" class="flex items-center gap-1.5 text-[11px] font-medium text-amber-600">
            <UIcon name="i-lucide-alert-circle" class="text-xs" />
            {{ rangeFilterError }}
          </span>
        </div>

        <!-- Spending period mode -->
        <div v-else class="flex flex-wrap items-center gap-3">
          <span class="text-xs font-semibold text-slate-500 mr-1">Période de dépenses :</span>
          <USelectMenu
            v-if="spendingPeriodItems.length"
            v-model="spendingPeriodId"
            :items="spendingPeriodItems"
            value-key="value"
            variant="outline"
            color="neutral"
            class="min-w-80"
            placeholder="Sélectionnez une période"
          />
          <span v-else class="text-xs text-slate-400">Aucune période de dépenses disponible</span>
        </div>
      </div>

      <!-- Target rule (read only, defined in settings) -->
      <div class="bg-white border border-slate-100 rounded-2xl px-5 py-4 shadow-sm mb-5">
        <div class="flex items-center justify-between gap-3 mb-4">
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-sliders-horizontal" class="text-slate-400 text-sm" />
            <span class="text-xs font-semibold text-slate-600">Règle cible</span>
          </div>
          <span class="text-[11px] text-slate-400">Modifiable dans vos paramètres de profil</span>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-5">
          <!-- Needs -->
          <div>
            <div class="flex justify-between mb-1.5">
              <span class="text-xs font-semibold text-indigo-600">Charges fixes</span>
              <span class="text-xs font-bold text-slate-700">{{ ruleConfig.needs }}%</span>
            </div>
            <div class="w-full h-1.5 rounded-full bg-indigo-100 overflow-hidden">
              <div class="h-full bg-indigo-500 rounded-full transition-all duration-300" :style="{ width: ruleConfig.needs + '%' }" />
            </div>
          </div>
          <!-- Wants -->
          <div>
            <div class="flex justify-between mb-1.5">
              <span class="text-xs font-semibold text-amber-600">Loisirs & envies</span>
              <span class="text-xs font-bold text-slate-700">{{ ruleConfig.wants }}%</span>
            </div>
            <div class="w-full h-1.5 rounded-full bg-amber-100 overflow-hidden">
              <div class="h-full bg-amber-500 rounded-full transition-all duration-300" :style="{ width: ruleConfig.wants + '%' }" />
            </div>
          </div>
          <!-- Savings -->
          <div>
            <div class="flex justify-between mb-1.5">
              <span class="text-xs font-semibold text-emerald-600">Épargne</span>
              <span class="text-xs font-bold text-slate-700">{{ ruleConfig.savings }}%</span>
            </div>
            <div class="w-full h-1.5 rounded-full bg-emerald-100 overflow-hidden">
              <div class="h-full bg-emerald-500 rounded-full transition-all duration-300" :style="{ width: ruleConfig.savings + '%' }" />
            </div>
          </div>
        </div>
        <!-- Visual rule bar -->
        <div class="mt-4 h-5 rounded-xl overflow-hidden flex shadow-inner">
          <div class="h-full bg-indigo-400 transition-all duration-300" :style="{ width: ruleConfig.needs + '%' }" />
          <div class="h-full bg-amber-400 transition-all duration-300" :style="{ width: ruleConfig.wants + '%' }" />
          <div class="h-full bg-emerald-400 transition-all duration-300" :style="{ width: ruleConfig.savings + '%' }" />
        </div>
      </div>

      <!-- Rule data -->
      <div v-if="isLoadingRule" class="flex items-center justify-center gap-3 py-20 text-slate-400">
        <div class="w-7 h-7 rounded-full border-2 border-slate-200 border-t-indigo-500 animate-spin" />
        <span class="text-sm">Chargement…</span>
      </div>

      <template v-else-if="ruleData">

        <!-- Income badge -->
        <div class="bg-white border border-slate-100 rounded-2xl px-5 py-3 shadow-sm mb-5 flex flex-wrap items-center gap-3">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-lg bg-slate-800 flex items-center justify-center">
              <UIcon name="i-lucide-banknote" class="text-white text-sm" />
            </div>
            <span class="text-xs font-semibold text-slate-500">Revenu de la période</span>
          </div>
          <span class="text-xl font-bold tracking-tight text-slate-900">{{ formatCurrency(ruleData.income) }}</span>
          <span class="ml-auto inline-flex items-center gap-1.5 text-[11px] font-medium text-slate-400">
            <UIcon name="i-lucide-filter" class="text-xs" />
            {{ ruleFilterLabel }}
          </span>
        </div>

        <!-- 3 Segment cards + bar chart -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-4">
          <div
            v-for="seg in ruleSegments"
            :key="seg.label"
            :class="['p-5 rounded-2xl border shadow-sm transition-all duration-200 hover:shadow-md', seg.light, seg.border]">
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-center gap-2.5">
                <div :class="['w-9 h-9 rounded-xl flex items-center justify-center text-white shadow-sm', seg.bg]">
                  <UIcon :name="seg.icon" class="text-base" />
                </div>
                <span :class="['text-[11px] uppercase tracking-wider font-semibold', seg.text]">{{ seg.label }}</span>
              </div>
              <!-- Status pill -->
              <span :class="['text-[10px] font-bold px-2 py-1 rounded-full', Math.abs(seg.ratio - seg.target) < 0.03 ? 'bg-slate-100 text-slate-500' : seg.ratio > seg.target && seg.label !== 'Épargne' ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-600']">
                {{ Math.abs(seg.ratio - seg.target) < 0.03 ? '≈ Cible' : seg.ratio > seg.target && seg.label !== 'Épargne' ? '⚠ Au dessus' : '✓ Dans la cible' }}
              </span>
            </div>

            <!-- Amount + ratio -->
            <div class="mb-4">
              <div class="text-2xl font-bold text-slate-900 tracking-tight mb-0.5">{{ formatCurrency(seg.amount) }}</div>
              <div class="text-[11px] text-slate-500">
                Réel : <span class="font-bold" :class="seg.text">{{ (seg.ratio * 100).toFixed(1) }}%</span>
                &nbsp;·&nbsp; Cible : <span class="font-semibold text-slate-600">{{ (seg.target * 100).toFixed(0) }}%</span>
              </div>
            </div>

            <!-- Progress vs target -->
            <div class="space-y-1.5">
              <div class="flex justify-between text-[10px] text-slate-400">
                <span>Réel</span><span>Cible</span>
              </div>
              <!-- Target track -->
              <div class="relative h-3 rounded-full bg-white/60 border border-white overflow-hidden shadow-inner">
                <!-- Actual bar -->
                <div class="absolute left-0 top-0 h-full rounded-full transition-all duration-700 opacity-90"
                  :style="{ width: Math.min(seg.targetPct, 130) + '%', background: seg.color }" />
                <!-- Target line -->
                <div class="absolute top-0 h-full w-0.5 bg-white/80" style="left: 100%" />
              </div>
              <div class="h-2 rounded-full overflow-hidden"
                :style="{ background: `${seg.color}22` }">
                <div class="h-full rounded-full transition-all duration-700"
                  :style="{ width: Math.min(seg.targetPct, 100) + '%', background: seg.color }" />
              </div>
            </div>
          </div>
        </div>

        <!-- Bar + Donut -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div class="lg:col-span-2 rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
              <h3 class="text-sm font-semibold text-slate-800">Comparaison Réel / Cible</h3>
            </div>
            <div class="p-5 h-56">
              <BarChart :data="ruleBarData" :options="chartOpts" />
            </div>
          </div>
          <div class="rounded-2xl bg-white border border-slate-100 shadow-sm overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
              <h3 class="text-sm font-semibold text-slate-800">Répartition réelle</h3>
            </div>
            <div class="p-5">
              <div class="h-40">
                <DoughnutChart :data="ruleDonutData" :options="donutOpts" />
              </div>
              <ul class="mt-4 flex flex-col gap-2">
                <li v-for="seg in ruleSegments" :key="seg.label" class="flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: seg.color }" />
                  <span class="flex-1 text-[11px] text-slate-500 truncate">{{ seg.label }}</span>
                  <span class="text-[11px] font-bold" :class="seg.text">{{ (seg.ratio * 100).toFixed(1) }}%</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

      </template>

      <div v-else class="flex flex-col items-center gap-2 py-16 text-slate-300 text-sm">
        <UIcon name="i-lucide-bar-chart-2" class="text-4xl" />
        <span>Aucune donnée disponible</span>
      </div>

    </div>
  </div>
</template>