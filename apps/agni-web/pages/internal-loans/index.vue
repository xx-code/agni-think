<script setup lang="ts">
import type { NuxtError } from '#app';
import { ModalEditInternalLoan } from '#components';
import { getLocalTimeZone } from '@internationalized/date';
import { fetchAccounts } from '~/composables/accounts/useAccounts';
import { fetchInternalLoans, useCreateInternalLoan } from '~/composables/internal-loan';
import type { EditInternalLoanType } from '~/types/ui/internal-loan';

type InternalLoanRow = {
    id: string
    fundAccountId: string
    fundAccountName: string
    creditCardAccountId: string
    creditCardAccountName: string
    dueDate: Date
}

const overlay = useOverlay()
const toast = useToast()
const modalInternalLoan = overlay.create(ModalEditInternalLoan)

const { data, refresh } = useAsyncData('internal-loans-pages', async () => {
    const res = await fetchInternalLoans({ offset: 0, limit: 0, queryAll: true })
    const accounts = await fetchAccounts({ offset: 0, limit: 0, queryAll: true })
    const getAccountName = (id: string) => {
        const acc = accounts.items.find(i => i.id === id)
        return acc ? acc.title : ''
    }
    return {
        internalLoans: res.items.map(i => ({
            id: i.id,
            fundAccountId: i.fundSourceId,
            fundAccountName: getAccountName(i.fundSourceId),
            creditCardAccountId: i.creditTargetId,
            creditCardAccountName: getAccountName(i.creditTargetId),
            dueDate: i.dueDate
        } satisfies InternalLoanRow)),
    }
})

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
        toast.add({ title: 'Succès', description: 'Prêt delete.', color: 'success' })
    } catch (err) {
        const nuxtErr = err as NuxtError
        toast.add({
            title: 'Erreur',
            description: 'Erreur lors de la soumission de internal loan: ' + nuxtErr.data,
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

function isDueSoon(date: Date) {
    const diff = new Date(date).getTime() - Date.now()
    return diff > 0 && diff < 7 * 24 * 60 * 60 * 1000
}

function isOverdue(date: Date) {
    return new Date(date).getTime() < Date.now()
}
</script>

<template>
    <div class="min-h-screen bg-slate-50 font-sans">

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
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
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
                    <div class="w-11 h-11 rounded-xl bg-amber-50 flex items-center justify-center shrink-0">
                        <svg class="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" stroke-width="2"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </div>
                    <div>
                        <p class="text-2xl font-bold text-slate-800">
                            {{ data?.internalLoans?.filter(l => isDueSoon(l.dueDate)).length ?? 0 }}
                        </p>
                        <p class="text-xs text-slate-500 mt-0.5">Échéances proches</p>
                    </div>
                </div>

                <div class="bg-white rounded-2xl border border-slate-200 p-5 flex items-center gap-4 shadow-sm">
                    <div class="w-11 h-11 rounded-xl bg-rose-50 flex items-center justify-center shrink-0">
                        <svg class="w-5 h-5 text-rose-500" fill="none" stroke="currentColor" stroke-width="2"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                    </div>
                    <div>
                        <p class="text-2xl font-bold text-slate-800">
                            {{ data?.internalLoans?.filter(l => isOverdue(l.dueDate)).length ?? 0 }}
                        </p>
                        <p class="text-xs text-slate-500 mt-0.5">En retard</p>
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
                                    Statut</th>
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

                                <!-- Status Badge -->
                                <td class="px-6 py-4">
                                    <span v-if="isOverdue(loan.dueDate)"
                                        class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-100 text-rose-700">
                                        <span class="w-1.5 h-1.5 rounded-full bg-rose-500 animate-pulse"></span>
                                        En retard
                                    </span>
                                    <span v-else-if="isDueSoon(loan.dueDate)"
                                        class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">
                                        <span class="w-1.5 h-1.5 rounded-full bg-amber-500"></span>
                                        Bientôt dû
                                    </span>
                                    <span v-else
                                        class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-700">
                                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                                        Actif
                                    </span>
                                </td>

                                <!-- Actions -->
                                <td class="px-6 py-4">
                                    <div
                                        class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-150">
                                        <button
                                            class="p-1.5 rounded-lg text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-colors"
                                            title="Modifier">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"
                                                viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                    d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                                            </svg>
                                        </button>
                                        <button @click="deleteInternalLoan(loan.id)"
                                            class="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                                            title="Supprimer">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"
                                                viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round"
                                                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                            </svg>
                                        </button>
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