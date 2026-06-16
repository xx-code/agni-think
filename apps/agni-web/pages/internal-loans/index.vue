<script setup lang="ts">
import type { NuxtError } from '#app';
import { ModalEditInternalLoan } from '#components';
import { getLocalTimeZone } from '@internationalized/date';
import { fetchAccounts } from '~/composables/api/accounts';
import { fetchInternalLoans, useCreateInternalLoan, useDeleteInternalLoan, useAddRefundInternalLoan, useRemoveRefundInternalLoan } from '~/composables/api/internal-loan';
import { fetchInvoicePagination } from '~/composables/api/invoices';
import type { EditInternalLoanType } from '~/types/ui/internal-loan';
import type { InvoiceType } from '~/types/ui/transaction';

type InternalLoanRow = {
    id: string
    fundAccountId: string
    fundAccountName: string
    creditCardAccountId: string
    creditCardAccountName: string
    dueDate: Date
    loanAmount: number
    refundAmount: number
    freezeInvoices: string[]
}

const overlay = useOverlay()
const toast = useToast()
const modalInternalLoan = overlay.create(ModalEditInternalLoan)

const { data, refresh } = useAsyncData('internal-loans-pages', async () => {
    const [res, accounts, freezeInvoicesRes] = await Promise.all([
        fetchInternalLoans({ offset: 0, limit: 0, queryAll: true }),
        fetchAccounts({ offset: 0, limit: 0, queryAll: true }),
        fetchInvoicePagination({ isFreeze: true, offset: 0, limit: 0, queryAll: true })
    ])
    const getAccountName = (id: string) => {
        const acc = accounts.items.find(i => i.id === id)
        return acc ? acc.title : ''
    }
    return {
        accounts: accounts.items,
        freezeInvoices: freezeInvoicesRes.items,
        internalLoans: res.items.map(i => ({
            id: i.id,
            fundAccountId: i.fundSourceId,
            fundAccountName: getAccountName(i.fundSourceId),
            creditCardAccountId: i.creditTargetId,
            creditCardAccountName: getAccountName(i.creditTargetId),
            dueDate: i.dueDate,
            loanAmount: i.loanAmount,
            refundAmount: i.refundAmount,
            freezeInvoices: i.freezeInvoices
        } satisfies InternalLoanRow)),
    }
})

const activeRefundLoanId = ref<string | null>(null)
const refundAccountId = ref('')
const refundAmount = ref(0)

function toggleRefundPanel(loanId: string) {
    if (activeRefundLoanId.value === loanId) {
        activeRefundLoanId.value = null
        return
    }
    activeRefundLoanId.value = loanId
    refundAccountId.value = ''
    refundAmount.value = 0
}

function loanFreezeInvoices(loan: InternalLoanRow): InvoiceType[] {
    if (!data.value?.freezeInvoices) return []
    return data.value.freezeInvoices.filter(f => loan.freezeInvoices.includes(f.id))
}

async function addRefund(loanId: string) {
    if (!refundAccountId.value || refundAmount.value <= 0) {
        toast.add({ title: 'Erreur', description: 'Sélectionnez un compte et entrez un montant.', color: 'error' })
        return
    }
    try {
        await useAddRefundInternalLoan(loanId, { refundAccountId: refundAccountId.value, refundAmount: refundAmount.value })
        activeRefundLoanId.value = null
        refresh()
        toast.add({ title: 'Succès', description: 'Remboursement ajouté.', color: 'success' })
    } catch (err) {
        const nuxtErr = err as NuxtError
        toast.add({ title: 'Erreur', description: nuxtErr.data?.message ?? 'Erreur lors de l\'ajout du remboursement.', color: 'error' })
    }
}

async function removeRefund(loanId: string, freezeInvoiceId: string) {
    try {
        await useRemoveRefundInternalLoan(loanId, { freezeInvoiceRefundId: freezeInvoiceId })
        refresh()
        toast.add({ title: 'Succès', description: 'Remboursement retiré.', color: 'success' })
    } catch (err) {
        const nuxtErr = err as NuxtError
        toast.add({ title: 'Erreur', description: nuxtErr.data?.message ?? 'Erreur lors du retrait du remboursement.', color: 'error' })
    }
}

async function onSubmitInternalLoan(edit: EditInternalLoanType) {
    try {
        await useCreateInternalLoan({
            fundAccountId: edit.fundSourceId,
            creditAccountId: edit.creditTargetId,
            transactionDate: edit.transactionDate.toDate(getLocalTimeZone()).toISOString(),
            dueDate: edit.dueDate.toDate(getLocalTimeZone()).toISOString(),
            transactions: edit.transactions.map(i => ({
                amount: i.amount,
                categoryId: i.categoryId,
                budgetIds: i.budgetIds,
                description: i.description,
                tagIds: i.tagIds
            })),
            deductions: edit.deductions.map(i => ({ deductionId: i.deductionId, amount: i.amount }))
        })
        refresh()
        toast.add({ title: 'Succès', description: 'Prêt interne créé avec succès.', color: 'success' })
    } catch (err) {
        const nuxtErr = err as NuxtError
        toast.add({
            title: 'Erreur',
            description: 'Erreur lors de la soumission de internal loan: ' + nuxtErr.data,
            color: 'error'
        })
    }
}

async function deleteInternalLoan(id: string) {
    try {
        await useDeleteInternalLoan(id)
        refresh()
        toast.add({ title: 'Succès', description: 'Prêt supprimé.', color: 'success' })
    } catch (err) {
        const nuxtErr = err as NuxtError
        toast.add({
            title: 'Erreur',
            description: 'Erreur lors de la suppression: ' + nuxtErr.data,
            color: 'error'
        })
    }
}

function openModalInternalLoan() {
    modalInternalLoan.open({ onSubmit: onSubmitInternalLoan })
}

function formatDate(date: Date) {
    return new Date(date).toLocaleDateString('fr-CA', { year: 'numeric', month: 'short', day: 'numeric' })
}

const progressStats = computed(() => {
    const loans = data.value?.internalLoans ?? []
    const totalLoan = loans.reduce((s, l) => s + l.loanAmount, 0)
    const totalRefund = loans.reduce((s, l) => s + l.refundAmount, 0)
    return { totalLoan, totalRefund, overallProgress: totalLoan > 0 ? (totalRefund / totalLoan) * 100 : 0 }
})

function refundProgress(loan: InternalLoanRow) {
    if (loan.loanAmount <= 0) return 0
    return Math.min((loan.refundAmount / loan.loanAmount) * 100, 100)
}

function formatMoney(amount: number) {
    return new Intl.NumberFormat('fr-CA', { style: 'currency', currency: 'CAD' }).format(amount)
}
</script>

<template>
    <div class="bg-slate-50 font-sans">

        <!-- Top Header Bar -->
        <header class="bg-white border-b border-slate-200 px-8 py-5 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center shadow-sm">
                    <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="2"
                        viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round"
                            d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                    </svg>
                </div>
                <div>
                    <h1 class="text-lg font-semibold text-slate-800 leading-tight">Prêts Internes</h1>
                    <p class="text-xs text-slate-400 leading-tight">Gestion des transferts de fonds</p>
                </div>
            </div>
            <button @click="openModalInternalLoan"
                class="inline-flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 active:bg-indigo-800 text-white text-sm font-medium px-4 py-2.5 rounded-xl shadow-sm transition-all duration-150">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                </svg>
                Nouveau prêt
            </button>
        </header>

        <main class="px-8 py-8 max-w-7xl mx-auto">

            <!-- Stats Cards Row -->
            <div class="grid grid-cols-1 sm:grid-cols-4 gap-4 mb-8">
                <div class="bg-white rounded-2xl border border-slate-200 p-5 flex items-center gap-4 shadow-sm">
                    <div class="w-11 h-11 rounded-xl bg-indigo-50 flex items-center justify-center shrink-0">
                        <svg class="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" stroke-width="2"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                    </div>
                    <div>
                        <p class="text-2xl font-bold text-slate-800">{{ data?.internalLoans?.length ?? 0 }}</p>
                        <p class="text-xs text-slate-500 mt-0.5">Total des prêts</p>
                    </div>
                </div>

                <div class="bg-white rounded-2xl border border-slate-200 p-5 flex items-center gap-4 shadow-sm">
                    <div class="w-11 h-11 rounded-xl bg-emerald-50 flex items-center justify-center shrink-0">
                        <svg class="w-5 h-5 text-emerald-500" fill="none" stroke="currentColor" stroke-width="2"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                        </svg>
                    </div>
                    <div>
                        <p class="text-2xl font-bold text-slate-800">{{ formatMoney(progressStats.totalLoan) }}</p>
                        <p class="text-xs text-slate-500 mt-0.5">Montant total prêté</p>
                    </div>
                </div>

                <div class="bg-white rounded-2xl border border-slate-200 p-5 flex items-center gap-4 shadow-sm">
                    <div class="w-11 h-11 rounded-xl bg-blue-50 flex items-center justify-center shrink-0">
                        <svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" stroke-width="2"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                        </svg>
                    </div>
                    <div>
                        <p class="text-2xl font-bold text-slate-800">{{ formatMoney(progressStats.totalRefund) }}</p>
                        <p class="text-xs text-slate-500 mt-0.5">Total remboursé</p>
                    </div>
                </div>

                <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
                    <div class="flex items-center gap-4 mb-2">
                        <div class="w-11 h-11 rounded-xl bg-amber-50 flex items-center justify-center shrink-0">
                            <svg class="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" stroke-width="2"
                                viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        </div>
                        <div>
                            <p class="text-2xl font-bold text-slate-800">{{ progressStats.overallProgress.toFixed(0) }}%
                            </p>
                            <p class="text-xs text-slate-500 mt-0.5">Progression globale</p>
                        </div>
                    </div>
                    <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                        <div class="h-full bg-amber-500 rounded-full transition-all duration-500"
                            :style="{ width: progressStats.overallProgress + '%' }">
                        </div>
                    </div>
                </div>
            </div>

            <!-- Table Card -->
            <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">

                <!-- Table Header -->
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h2 class="text-sm font-semibold text-slate-700">Liste des prêts</h2>
                    <span class="text-xs text-slate-400 bg-slate-100 px-2.5 py-1 rounded-full font-medium">
                        {{ data?.internalLoans?.length ?? 0 }} entrées
                    </span>
                </div>

                <!-- Empty State -->
                <div v-if="!data?.internalLoans?.length" class="py-20 flex flex-col items-center text-center gap-3">
                    <div class="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center">
                        <svg class="w-7 h-7 text-slate-400" fill="none" stroke="currentColor" stroke-width="1.5"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M2.25 8.25h19.5M2.25 9h19.5m-16.5 5.25h6m-6 2.25h3m-3.75 3h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5z" />
                        </svg>
                    </div>
                    <div>
                        <p class="text-sm font-semibold text-slate-700">Aucun prêt interne</p>
                        <p class="text-xs text-slate-400 mt-1">Créez votre premier prêt en cliquant sur « Nouveau prêt
                            ».</p>
                    </div>
                    <button @click="openModalInternalLoan"
                        class="mt-2 text-sm text-indigo-600 hover:text-indigo-700 font-medium underline underline-offset-2">
                        Créer un prêt
                    </button>
                </div>

                <!-- Table -->
                <div v-else class="overflow-x-auto">
                    <table class="w-full text-sm">
                        <thead>
                            <tr class="bg-slate-50 border-b border-slate-100">
                                <th
                                    class="text-left px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">
                                    Compte source</th>
                                <th
                                    class="text-left px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">
                                    Carte de crédit</th>
                                <th
                                    class="text-left px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">
                                    Échéance</th>
                                <th
                                    class="text-left px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">
                                    Progression</th>
                                <th class="px-6 py-3"></th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
                            <tr v-for="loan in data.internalLoans" :key="loan.id"
                                class="hover:bg-slate-50/70 transition-colors duration-100 group">

                                <!-- Fund Account -->
                                <td class="px-6 py-4">
                                    <div class="flex items-center gap-3">
                                        <div
                                            class="w-8 h-8 rounded-lg bg-indigo-100 flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4 text-indigo-600" fill="none" stroke="currentColor"
                                                stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                    d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p class="font-medium text-slate-800 leading-tight">{{ loan.fundAccountName
                                                }}</p>
                                            <p class="text-xs text-slate-400 leading-tight mt-0.5">Fonds source</p>
                                        </div>
                                    </div>
                                </td>

                                <!-- Credit Card Account -->
                                <td class="px-6 py-4">
                                    <div class="flex items-center gap-3">
                                        <div
                                            class="w-8 h-8 rounded-lg bg-violet-100 flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4 text-violet-600" fill="none" stroke="currentColor"
                                                stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                    d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p class="font-medium text-slate-800 leading-tight">{{
                                                loan.creditCardAccountName }}</p>
                                            <p class="text-xs text-slate-400 leading-tight mt-0.5">Carte de crédit</p>
                                        </div>
                                    </div>
                                </td>

                                <!-- Due Date -->
                                <td class="px-6 py-4">
                                    <p class="text-slate-700 font-medium">{{ formatDate(loan.dueDate) }}</p>
                                </td>

                                <!-- Progress -->
                                <td class="px-6 py-4 min-w-[140px]">
                                    <div class="flex flex-col gap-1.5">
                                        <div class="flex items-center justify-between text-xs">
                                            <span class="text-slate-500">Remboursé</span>
                                            <span class="font-semibold text-slate-700">{{ refundProgress(loan).toFixed(0) }}%</span>
                                        </div>
                                        <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                                            <div class="h-full rounded-full transition-all duration-500"
                                                :class="refundProgress(loan) >= 100 ? 'bg-emerald-500' : refundProgress(loan) >= 50 ? 'bg-indigo-500' : 'bg-amber-500'"
                                                :style="{ width: refundProgress(loan) + '%' }">
                                            </div>
                                        </div>
                                        <div class="flex items-center justify-between text-xs text-slate-400">
                                            <span>{{ formatMoney(loan.refundAmount) }}</span>
                                            <span>{{ formatMoney(loan.loanAmount) }}</span>
                                        </div>
                                    </div>
                                </td>

                                <!-- Actions -->
                                <td class="px-6 py-4 relative">
                                    <div class="flex items-center justify-end gap-1">
                                        <button @click="toggleRefundPanel(loan.id)"
                                            class="p-2 rounded-lg text-indigo-600 hover:bg-indigo-50 transition-colors"
                                            title="Gérer les remboursements">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"
                                                viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                    d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                            </svg>
                                        </button>
                                        <button @click="deleteInternalLoan(loan.id)"
                                            class="p-2 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                                            title="Supprimer">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"
                                                viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                            </svg>
                                        </button>
                                    </div>

                                    <!-- Refund Panel -->
                                    <div v-if="activeRefundLoanId === loan.id"
                                        class="absolute right-0 top-full mt-1 z-20 w-80 bg-white rounded-2xl border border-slate-200 shadow-xl p-4"
                                        @click.stop>
                                        <div class="flex items-center justify-between mb-3">
                                            <h3 class="text-sm font-semibold text-slate-700">Gestion des remboursements</h3>
                                            <button @click="activeRefundLoanId = null"
                                                class="p-0.5 rounded-md text-slate-400 hover:text-slate-600 hover:bg-slate-100">
                                                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"
                                                    viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                                                </svg>
                                            </button>
                                        </div>

                                        <!-- Add Refund -->
                                        <div class="mb-4">
                                            <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">Ajouter un remboursement</p>
                                            <select v-model="refundAccountId"
                                                class="w-full mb-2 px-3 py-2 text-sm border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none">
                                                <option value="" disabled>Sélectionner un compte</option>
                                                <option v-for="acc in data?.accounts ?? []" :key="acc.id" :value="acc.id">
                                                    {{ acc.title }}
                                                </option>
                                            </select>
                                            <div class="flex items-center gap-2">
                                                <input v-model.number="refundAmount" type="number" step="0.01" min="0"
                                                    placeholder="Montant"
                                                    class="flex-1 px-3 py-2 text-sm border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none" />
                                                <button @click="addRefund(loan.id)"
                                                    class="px-3 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl transition-colors">
                                                    Ajouter
                                                </button>
                                            </div>
                                        </div>

                                        <!-- Existing Refunds -->
                                        <div>
                                            <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">Remboursements existants</p>
                                            <div v-if="loanFreezeInvoices(loan).length === 0"
                                                class="text-xs text-slate-400 py-2 text-center">
                                                Aucun remboursement
                                            </div>
                                            <div v-else class="space-y-1 max-h-40 overflow-y-auto">
                                                <div v-for="fi in loanFreezeInvoices(loan)" :key="fi.id"
                                                    class="flex items-center justify-between px-2 py-1.5 rounded-lg bg-slate-50">
                                                    <div class="text-xs">
                                                        <p class="font-medium text-slate-700">{{ fi.transactions?.[0]?.description ?? 'Remboursement' }}</p>
                                                        <p class="text-slate-400">{{ formatMoney(fi.total) }}</p>
                                                    </div>
                                                    <button @click="removeRefund(loan.id, fi.id)"
                                                        class="p-1 rounded-md text-rose-400 hover:text-rose-600 hover:bg-rose-50">
                                                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2"
                                                            viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                        </svg>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </td>

                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Table Footer -->
                <div v-if="data?.internalLoans?.length"
                    class="px-6 py-3 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between">
                    <p class="text-xs text-slate-400">{{ data.internalLoans.length }} prêt(s) au total</p>
                    <div class="flex items-center gap-1 text-xs text-slate-400">
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                        Mis à jour maintenant
                    </div>
                </div>

            </div>
        </main>
    </div>
</template>
