<script setup lang="ts">
import { computed, ref } from "vue";
import { ALL_ACCOUNT_ID } from "~/composables/accounts/useAccountsWithPastBalance";
import useDeleteAccount from "~/composables/accounts/useDeleteAccount";
import type { AccountBrokeDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType, EditAccountType } from "~/types/ui/account";
import useCreateAccount from "~/composables/accounts/useCreateAccount";
import useUpdateAccount from "~/composables/accounts/useUpdateAccount";
import useTransactionPagination from "~/composables/transactions/useTransactionPagination";
import { ModalEditAccount, ModalEditFreezeTransaction, ModalEditTransaction, ModalEditTransfer, ModalQuickTransView } from "#components";
import { fetchAccount, fetchAccountWithDetail } from "~/composables/accounts/useAccount";
import type { EditFreezeTransactionType, EditTransactionType, EditTransfertType, TransactionTableType, TransactionType } from "~/types/ui/transaction";
import useUpdateTransaction from "~/composables/transactions/useUpdateTransaction";
import useCreateTransaction from "~/composables/transactions/useCreateTransaction";
import { fetchTransaction } from "~/composables/transactions/useTransaction";
import useFreezeTransaction from "~/composables/transactions/useFreezeTransaction";
import useTransfertTransaction from "~/composables/transactions/useTransfertTransaction";
import type { FilterTransactionQuery } from "~/types/api/transaction";
import useCategories from "~/composables/categories/useCategories";
import useTags from "~/composables/tags/useTags";
import { getLocalTimeZone } from "@internationalized/date";
import useBudgets from "~/composables/budgets/useBudgets";
import useAnalyseBudgetRules from "~/composables/analytics/useBudgetRules";
import { fetchAccountsWithDetail } from "~/composables/accounts/useAccounts";
import { fetchAccountTypes } from "~/composables/internals/useAccountTypes";
import type { NuxtError } from "#app";

type AccountByType = {
    id: string
    title: string
    accounts: AccountWithDetailType[]
}

const { data: accounts, refresh: refreshAccounts } = await useAsyncData('accounts+categories+tags+budgets', async () => {
    const res = await fetchAccountsWithDetail({ offset: 0, limit: 0, queryAll: true })
    const accountTypes = await fetchAccountTypes()

    const accountsByType = []
    for(const type of accountTypes) {

        const accounts = res.items.filter(i => i.type === type.id)
        accountsByType.push({
            id: type.id,
            title: type.value,
            accounts: accounts
        } satisfies AccountByType) 
    }

    return accountsByType
})

const totalAccountBalance = computed(() => {
    let total = 0 
    let totalFreezed = 0
    let totalLocked = 0

    if (accounts.value) {
        for(const acc of accounts.value) {
            if (acc.id !== 'Saving' && acc.id !== 'Broking')
                total += acc.accounts.reduce((acc, curr) => acc += curr.balance, 0)
            totalFreezed += acc.accounts.reduce((acc, curr) => acc += curr.freezedBalance, 0)
            totalLocked += acc.accounts.reduce((acc, curr) => acc += curr.lockedBalance, 0)
        }
    }  

    return { totalBalance: total, totalFreezedBalance: totalFreezed, totalLockedBalance: totalLocked }
})

const {data: categories, error: errorCategories, refresh: refreshCategories} = useCategories({
    limit: 0, offset: 0, queryAll: true
});
const {data: tags, error: errorTags, refresh: refreshTag} = useTags({
    limit: 0, offset: 0, queryAll: true
});
const {data: budgets, error: errorBudgets, refresh: refreshBudget } = useBudgets({
    limit: 0, offset: 0, queryAll: true
});

const selectedAccountId = ref(ALL_ACCOUNT_ID);


const paramsTransaction = reactive<FilterTransactionQuery>({offset: 0, limit: 4, isFreeze: false});

const overlay = useOverlay();
const modalAccount = overlay.create(ModalEditAccount);
const modalTransfer = overlay.create(ModalEditTransfer);
const modalTransaction = overlay.create(ModalEditTransaction);
const modalFreezeTransaction = overlay.create(ModalEditFreezeTransaction);
const slideTransactions = overlay.create(ModalQuickTransView)

const onSelectAccount = (id: string) => {
    selectedAccountId.value = id
    if (id !== ALL_ACCOUNT_ID) {
        paramsTransaction.accountFilterIds = [id]
    }

    onUpateAccount(id)
}

const toast = useToast();
const onSaveAccount = async (value: EditAccountType, oldValue?: AccountType) => {
    try {
        if (oldValue)
            await useUpdateAccount(oldValue.id, value);
        else 
            await useCreateAccount(value);
        
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
        account = await fetchAccountWithDetail(accountId);
    }
        
    modalAccount.open({
        account: account,
        onSubmit: onSaveAccount 
    }); 
}

async function onTransfertAccount(value: EditTransfertType) {
    try {
        await useTransfertTransaction({
            accountIdFrom: value.accountIdFrom,
            accountIdTo: value.accountIdTo,
            amount: value.amount,
            date: value.date.toString()
        })
        refreshAccounts()
    } catch(err) {
        toast.add({
            title: 'Error tranfert',
            description: 'Error while transfert account',
            color: 'error'
        });
    }
} 

async function openModalTransferAccount (accountId?: string){ 
    const filterId = accountId === ALL_ACCOUNT_ID ? undefined : accountId
        
    const instance = modalTransfer.open({
        accountId: filterId,
        onSubmit: onTransfertAccount 
    });

    const shouldRefresh = await instance.result; 
}

async function onSubmitTransaction(value: EditTransactionType, oldValue?: TransactionType) {
    try {
         if (oldValue) {
            const recordRemovedIds = oldValue.records.filter(i => value.records.find(
                v => 
                    v.amount === i.amount && 
                    v.categoryId === i.categoryId &&
                    v.budgetIds.every(b => i.budgetRefs.includes(b)) &&
                    v.tagIds.every(t => i.tagRefs.includes(t)) &&
                    v.description === i.description
            ) !== undefined).map(i => i.id)

            const recordAdded = value.records.filter(i => oldValue.records.find(
                v => 
                    v.amount === i.amount && 
                    v.categoryId === i.categoryId &&
                    v.budgetRefs.every(b => i.budgetIds.includes(b)) &&
                    v.tagRefs.every(t => i.tagIds.includes(t)) &&
                    v.description === i.description
            ) !== undefined)

            await useUpdateTransaction(oldValue.id, {
                addRecords: recordAdded, 
                removeRecordIds: recordRemovedIds,
                deductions: [],
                id: oldValue.id,
                accountId: value.accountId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                type: value.type
            });
        } else {
            await useCreateTransaction({
                accountId: value.accountId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                type: value.type,
                status: value.state,
                records: value.records.map(i => ({
                    amount: i.amount,
                    categoryId: i.categoryId,
                    budgetIds: i.budgetIds,
                    description: i.description,
                    tagIds: i.tagIds
                })),
                deductions: []
            });
        }
        refreshAccounts()
    } catch(err) {
        toast.add({
            title: 'Error submit transaction',
            description: 'Error while submit transaction ' + err,
            color: 'error'
        })
    }
}

async function openModalEditTransaction(accountId?: string) {
    const instant = modalTransaction.open({
        transaction: undefined,
        accountSelectedId: accountId,
        onSubmit: onSubmitTransaction 
    });

    const shouldRefresh = await instant.result
}

async function onFreezeTransaction(value: EditFreezeTransactionType) {
    try {
        await useFreezeTransaction({
            accountId: value.accountId,
            title: value.title,
            amount: value.amount,
            endDate: value.endDate.toString()
        })
        refreshAccounts()
    } catch(err) {
        toast.add({
            title: 'Error Freeze',
            description: 'Error while freeze account',
            color: 'error'
        });
    }
}

async function openModalEditFreezeTransaction(accountId: string = '') {
    const instance = modalFreezeTransaction.open({
        onSubmit: onFreezeTransaction
    });

    await instance.result; 

}

const onDeleteAccount = async (accountId: string) => {
    const doDelete = confirm();
    if (doDelete) {
        await useDeleteAccount(accountId);
        refreshAccounts();
    }
}

const onUpateAccount = async (payload: string) => {
    let filterAcc: string[] = []
    if (payload !== ALL_ACCOUNT_ID)
        filterAcc = [payload] 
}

const computeAllUtilization = (accounts: AccountWithDetailType[]) => {
    const creditCardAccount = accounts.filter(i => i.type === 'CreditCard')
    const sum = creditCardAccount.reduce((acc, account) => acc += (account.detail as AccountCreditDetailType).creditUtilisation, 0)

    return (sum/creditCardAccount.length).toFixed(2)
}

const openTransactionViews = async (accountId: string) => {
    try {
        let account = await fetchAccountWithDetail(accountId);
            
        slideTransactions.open({
            account: account
        });
    } catch (err) {
        console.log(err)
    } 
}

const availableBalance = computed(() => {
    return totalAccountBalance.value.totalBalance - totalAccountBalance.value.totalFreezedBalance - totalAccountBalance.value.totalLockedBalance
})

const lockedPercentage = computed(() => {
    if (totalAccountBalance.value.totalBalance === 0) return 0
    return ((totalAccountBalance.value.totalFreezedBalance + totalAccountBalance.value.totalLockedBalance) / totalAccountBalance.value.totalBalance) * 100
})

</script>

<template>
    <div class="wallet-container">
        <!-- Header avec bouton d'ajout -->
        <div class="wallet-header">
            <h1 class="wallet-title">Mon Portefeuille</h1>
            <UButton 
                icon="i-lucide-plus" 
                size="lg" 
                variant="solid" 
                color="primary"
                @click="openAccountModal()">
                Ajouter un compte
            </UButton>
        </div>

        <!-- Cartes principales -->
        <div class="main-cards-grid">
            <!-- Carte Balance Totale -->
            <div class="balance-card">
                <div class="card-header">
                    <div>
                        <h3 class="card-label">Balance Totale</h3>
                        <p class="card-sublabel">Vue d'ensemble de vos finances</p>
                    </div>
                    <div class="icon-wrapper balance-icon">
                        <Icon name="i-lucide-wallet" class="text-2xl" />
                    </div>
                </div>

                <div class="balance-content">
                    <AmountTitle 
                        :amount="totalAccountBalance.totalBalance"
                        :sign="'$'"
                        class="main-amount"
                    />
                    
                    <div class="balance-details">
                        <div class="balance-item available">
                            <Icon name="i-lucide-circle-check" class="balance-item-icon" />
                            <span class="balance-item-label">Disponible:</span>
                            <span class="balance-item-value">${{ availableBalance.toFixed(2) }}</span>
                        </div>
                        <div class="balance-item freezed">
                            <Icon name="i-lucide-snowflake" class="balance-item-icon" />
                            <span class="balance-item-label">Gelé:</span>
                            <span class="balance-item-value">${{ totalAccountBalance.totalFreezedBalance.toFixed(2) }}</span>
                        </div>
                        <div class="balance-item locked">
                            <Icon name="i-lucide-lock" class="balance-item-icon" />
                            <span class="balance-item-label">Verrouillé:</span>
                            <span class="balance-item-value">${{ totalAccountBalance.totalLockedBalance.toFixed(2) }}</span>
                        </div>
                    </div>
                </div>

                <!-- Actions rapides -->
                <div class="quick-actions">
                    <UButton 
                        icon="i-lucide-banknote" 
                        size="sm" 
                        variant="soft"
                        color="success"
                        @click="openModalEditTransaction()">
                        Transaction
                    </UButton>
                    <UButton 
                        icon="i-lucide-arrow-right-left" 
                        size="sm" 
                        variant="soft"
                        color="info"
                        @click="openModalTransferAccount(selectedAccountId)">
                        Transfert
                    </UButton>
                    <UButton 
                        icon="i-lucide-snowflake" 
                        size="sm" 
                        variant="soft"
                        color="neutral"
                        @click="openModalEditFreezeTransaction(selectedAccountId)">
                        Geler
                    </UButton>
                </div>

                <!-- Barre de progression -->
                <div class="progress-section">
                    <div class="progress-bar-wrapper">
                        <div class="progress-bar-bg">
                            <div 
                                class="progress-bar-fill" 
                                :style="{ width: `${lockedPercentage}%` }" 
                            />
                        </div>
                    </div>
                    <div class="progress-labels">
                        <span class="progress-label">
                            <span class="progress-dot available"></span>
                            Disponible ({{ (100 - lockedPercentage).toFixed(1) }}%)
                        </span>
                        <span class="progress-label">
                            <span class="progress-dot locked"></span>
                            Bloqué ({{ lockedPercentage.toFixed(1) }}%)
                        </span>
                    </div>
                </div>
            </div>

            <!-- Carte Suggestions IA -->
            <div class="ai-suggestions-card">
                <div class="card-header">
                    <div>
                        <h3 class="card-label">Assistant Financier IA</h3>
                        <p class="card-sublabel">Recommandations personnalisées</p>
                    </div>
                    <div class="icon-wrapper ai-icon">
                        <Icon name="i-lucide-sparkles" class="text-2xl" />
                    </div>
                </div>

                <div class="ai-content">
                    <!-- Statistique principale -->
                    <div class="ai-stat-card">
                        <div class="ai-stat-header">
                            <Icon name="i-lucide-trending-down" class="ai-stat-icon" />
                            <span class="ai-stat-label">Évolution des dépenses</span>
                        </div>
                        <div class="ai-stat-value">
                            <span class="loading-shimmer">Analyse en cours...</span>
                        </div>
                        <div class="ai-stat-description">
                            vs. mois dernier
                        </div>
                    </div>

                    <!-- Suggestions -->
                    <div class="ai-suggestions-list">
                        <div class="ai-suggestion-item">
                            <Icon name="i-lucide-lightbulb" class="suggestion-icon tip" />
                            <div class="suggestion-content">
                                <p class="suggestion-title">Optimisation détectée</p>
                                <p class="suggestion-text loading-shimmer-text">Analyse de vos habitudes de dépenses...</p>
                            </div>
                        </div>

                        <div class="ai-suggestion-item">
                            <Icon name="i-lucide-alert-circle" class="suggestion-icon warning" />
                            <div class="suggestion-content">
                                <p class="suggestion-title">Alerte budget</p>
                                <p class="suggestion-text loading-shimmer-text">Vérification des limites de crédit...</p>
                            </div>
                        </div>

                        <div class="ai-suggestion-item">
                            <Icon name="i-lucide-target" class="suggestion-icon goal" />
                            <div class="suggestion-content">
                                <p class="suggestion-title">Objectif d'épargne</p>
                                <p class="suggestion-text loading-shimmer-text">Calcul de votre potentiel d'épargne...</p>
                            </div>
                        </div>
                    </div>

                    <!-- CTA -->
                    <UButton 
                        block 
                        size="md"
                        variant="solid"
                        color="primary"
                        class="ai-cta-button">
                        <Icon name="i-lucide-sparkles" class="mr-2" />
                        Voir toutes les recommandations
                    </UButton>
                </div>
            </div>
        </div>
        
        <!-- Liste des comptes par type -->
        <div class="accounts-section">
            <div v-for="group in accounts" :key="group.id" class="account-group">
                <div class="account-group-header" v-if="group.accounts.length > 0">
                    <h2 class="account-group-title">{{ group.title }}</h2>
                    <div v-if="group.id === 'CreditCard'" class="credit-utilization">
                        <Icon name="i-lucide-credit-card" class="mr-1" />
                        <span>Utilisation moyenne:</span>
                        <strong :style="{color: creditUilisationToColor(Number(computeAllUtilization(group.accounts))) }">
                            {{ computeAllUtilization(group.accounts) }}%
                        </strong>
                    </div>
                </div>
                
                <div class="accounts-carousel">
                    <div v-for="account in group.accounts" :key="account.id" class="account-card-wrapper">
                        <CardResumeAccount 
                            @open="openTransactionViews(account.id)"
                            v-if="account.id !== selectedAccountId"
                            :id="account.id"
                            :title="account.title"
                            :balance="account.balance"
                            :diff-past-balance-per="0"
                            :is-positif="true"
                            :freezed-balance="account.freezedBalance"
                            :locked-balance="account.lockedBalance"
                            :allow-edit="account.id !== ALL_ACCOUNT_ID"
                            :allow-delete="account.id !== ALL_ACCOUNT_ID" 
                            @add="openModalEditTransaction(account.id)"
                            @edit="openAccountModal(account.id)"
                            @delete="onDeleteAccount(account.id)"> 

                            <div v-if="account.type === 'CreditCard'" class="credit-card-details">
                                <p class="credit-limit">
                                    <Icon name="i-lucide-landmark" class="inline mr-1" />
                                    Limite: ${{ (account.detail as AccountCreditDetailType).creditLimit }}
                                </p>
                                <p 
                                    class="credit-usage" 
                                    :style="{color: creditUilisationToColor((account.detail as AccountCreditDetailType).creditUtilisation)}">
                                    <Icon name="i-lucide-percent" class="inline mr-1" />
                                    Utilisation: {{ (account.detail as AccountCreditDetailType).creditUtilisation }}%
                                </p>
                            </div>
                        </CardResumeAccount>
                    </div>
                </div> 
            </div>
        </div> 
    </div> 
</template>

<style scoped lang="scss">
.wallet-container {
    padding: 1.5rem;
    max-width: 1400px;
    margin: 0 auto;
}

.wallet-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;

    .wallet-title {
        font-size: 2rem;
        font-weight: 800;
        color: #1e293b;
        margin: 0;
    }
}

.main-cards-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2.5rem;

    @media (max-width: 768px) {
        grid-template-columns: 1fr;
    }
}

.balance-card,
.ai-suggestions-card {
    background: white;
    border-radius: 1rem;
    padding: 1.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e2e8f0;
    transition: all 0.3s ease;

    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
    }
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1.5rem;

    .card-label {
        font-size: 0.875rem;
        font-weight: 600;
        color: #64748b;
        margin: 0 0 0.25rem 0;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    .card-sublabel {
        font-size: 0.75rem;
        color: #94a3b8;
        margin: 0;
    }

    .icon-wrapper {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.balance-icon {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        &.ai-icon {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
        }
    }
}

.balance-content {
    .main-amount {
        font-size: 2.5rem;
        font-weight: 800;
        color: #1e293b;
        margin-bottom: 1rem;
    }

    .balance-details {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        margin-bottom: 1.5rem;

        .balance-item {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.5rem;
            border-radius: 0.5rem;
            background: #f8fafc;

            .balance-item-icon {
                font-size: 1rem;
            }

            .balance-item-label {
                font-size: 0.875rem;
                color: #64748b;
            }

            .balance-item-value {
                font-size: 0.875rem;
                font-weight: 600;
                margin-left: auto;
            }

            &.available {
                .balance-item-icon,
                .balance-item-value {
                    color: #10b981;
                }
            }

            &.freezed {
                .balance-item-icon,
                .balance-item-value {
                    color: #06b6d4;
                }
            }

            &.locked {
                .balance-item-icon,
                .balance-item-value {
                    color: #f59e0b;
                }
            }
        }
    }
}

.quick-actions {
    display: flex;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
    flex-wrap: wrap;
}

.progress-section {
    .progress-bar-wrapper {
        margin-bottom: 0.75rem;

        .progress-bar-bg {
            width: 100%;
            height: 8px;
            background: #e2e8f0;
            border-radius: 999px;
            overflow: hidden;

            .progress-bar-fill {
                height: 100%;
                background: linear-gradient(90deg, #f59e0b 0%, #ef4444 100%);
                border-radius: 999px;
                transition: width 0.5s ease;
            }
        }
    }

    .progress-labels {
        display: flex;
        justify-content: space-between;
        font-size: 0.75rem;

        .progress-label {
            display: flex;
            align-items: center;
            gap: 0.375rem;
            color: #64748b;

            .progress-dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;

                &.available {
                    background: #10b981;
                }

                &.locked {
                    background: #f59e0b;
                }
            }
        }
    }
}

.ai-content {
    .ai-stat-card {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 0.75rem;
        padding: 1.25rem;
        margin-bottom: 1.5rem;
        color: white;

        .ai-stat-header {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            margin-bottom: 0.75rem;

            .ai-stat-icon {
                font-size: 1.25rem;
            }

            .ai-stat-label {
                font-size: 0.875rem;
                opacity: 0.9;
            }
        }

        .ai-stat-value {
            font-size: 1.75rem;
            font-weight: 700;
            margin-bottom: 0.25rem;
        }

        .ai-stat-description {
            font-size: 0.75rem;
            opacity: 0.8;
        }
    }

    .ai-suggestions-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        margin-bottom: 1.5rem;

        .ai-suggestion-item {
            display: flex;
            gap: 0.75rem;
            padding: 1rem;
            background: #f8fafc;
            border-radius: 0.75rem;
            border: 1px solid #e2e8f0;

            .suggestion-icon {
                font-size: 1.5rem;
                flex-shrink: 0;

                &.tip {
                    color: #3b82f6;
                }

                &.warning {
                    color: #f59e0b;
                }

                &.goal {
                    color: #10b981;
                }
            }

            .suggestion-content {
                flex: 1;

                .suggestion-title {
                    font-size: 0.875rem;
                    font-weight: 600;
                    color: #1e293b;
                    margin: 0 0 0.25rem 0;
                }

                .suggestion-text {
                    font-size: 0.8125rem;
                    color: #64748b;
                    margin: 0;
                    line-height: 1.5;
                }
            }
        }
    }

    .ai-cta-button {
        background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
        border: none;
        font-weight: 600;

        &:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
        }
    }
}

// Effet de chargement
.loading-shimmer,
.loading-shimmer-text {
    background: linear-gradient(90deg, 
        rgba(255, 255, 255, 0.8) 25%, 
        rgba(255, 255, 255, 0.4) 50%, 
        rgba(255, 255, 255, 0.8) 75%
    );
    background-size: 200% 100%;
    animation: shimmer 2s infinite;
    border-radius: 0.25rem;
    display: inline-block;
    color: transparent;
}

.loading-shimmer {
    width: 100px;
    height: 1.75rem;
}

.loading-shimmer-text {
    width: 100%;
    height: 1em;
}

@keyframes shimmer {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}

.accounts-section {
    .account-group {
        margin-bottom: 2rem;

        .account-group-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
            padding-bottom: 0.75rem;
            border-bottom: 2px solid #e2e8f0;

            .account-group-title {
                font-size: 1.25rem;
                font-weight: 700;
                color: #1e293b;
                margin: 0;
            }

            .credit-utilization {
                display: flex;
                align-items: center;
                gap: 0.5rem;
                font-size: 0.875rem;
                color: #64748b;

                strong {
                    font-weight: 600;
                }
            }
        }

        .accounts-carousel {
            display: flex;
            gap: 1rem;
            overflow-x: auto;
            padding-bottom: 1rem;
            scroll-snap-type: x mandatory;

            &::-webkit-scrollbar {
                height: 8px;
            }

            &::-webkit-scrollbar-track {
                background: #f1f5f9;
                border-radius: 4px;
            }

            &::-webkit-scrollbar-thumb {
                background: #cbd5e1;
                border-radius: 4px;

                &:hover {
                    background: #94a3b8;
                }
            }

            .account-card-wrapper {
                scroll-snap-align: start;
                flex-shrink: 0;
                width: 280px;
            }
        }
    }
}

.credit-card-details {
    margin-top: 0.75rem;
    padding-top: 0.75rem;
    border-top: 1px solid #e2e8f0;

    p {
        font-size: 0.8125rem;
        margin: 0.375rem 0;
        display: flex;
        align-items: center;

        &.credit-limit {
            color: #64748b;
        }

        &.credit-usage {
            font-weight: 600;
        }
    }
}
</style>