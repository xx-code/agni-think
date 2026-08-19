<script setup lang="ts">
import { AccountType } from '~/types/constants/account';
import type { AccountCheckingDetailType, AccountCreditDetailType, QuickViewTransactionBalanceInfo } from '~/types/ui/account';

const { accountInfo } = defineProps<{
    accountInfo: QuickViewTransactionBalanceInfo
}>()

const availableBalance = computed(() => {
    return accountInfo.balance + Math.abs(accountInfo.freezedBalance + accountInfo.lockedBalance) 
})

function formatAccountBuffer(detail: AccountCheckingDetailType): number {
    return roundNumber(detail.buffer) 
}

function isBufferValid(buffer: number, balance: number): boolean {
    return balance >= buffer 
}

</script>

<template>
    <div>
        <div class="flex items-center flex-wrap gap-2 font-semibold text-sm">
            <div class="flex items-center">
                <Icon name="i-lucide-circle-check" class="text-green-500" />
                <span class="ml-1">Disponible</span>
                <span class="ml-1">{{ formatCurrency(availableBalance) }}</span>
            </div>
            
            <div class="flex items-center">
                <Icon name="i-lucide-snowflake" class="text-blue-300" />
                <span class="ml-1">Gelé</span>
                <span class="ml-1">{{ formatCurrency(accountInfo.freezedBalance) }}</span>
            </div>

            <div class="flex items-center">
                <Icon name="i-lucide-lock-keyhole" class="text-gray-500" />
                <span class="ml-1">Verrouillé</span>
                <span class="ml-1">{{ formatCurrency(accountInfo.lockedBalance) }}</span>
            </div>
        </div>

        <div v-if="accountInfo.type === AccountType.CreditCard" class="text-sm mt-2">
            <span class="text-sm text-gray-400">
                Prochaine facture: {{ formatDate((accountInfo.detail as AccountCreditDetailType).nextInvoicePaymentDate) }}
            </span>
            <div class="flex items-center text-sm">
                <Icon name="i-lucide-landmark" class="inline mr-1" />
                <span>
                    Limite: ${{ (accountInfo.detail as AccountCreditDetailType).creditLimit }}
                </span>
            </div>
            <div 
                class="flex items-center text-sm" 
                :style="{color: creditUilisationToColor((accountInfo.detail as AccountCreditDetailType).creditUtilisation)}">
                <span>
                    Credit utilisation: {{ (accountInfo.detail as AccountCreditDetailType).creditUtilisation }}%
                </span>
            </div>
        </div>

        <div v-else-if="accountInfo.type === AccountType.Checking" class="text-sm mt-2">
            <div class="flex items-center gap-1">
                <div class="flex items-center">
                    <Icon 
                        name="i-lucide-align-horizontal-space-around" 
                        :class="[isBufferValid(formatAccountBuffer(accountInfo.detail as AccountCheckingDetailType), accountInfo.balance + accountInfo.freezedBalance) ? 'text-green-500' : 'text-red-500']" />
                    <span class="ml-1">Buffer</span>
                </div>

                <span>${{ formatCurrency((accountInfo.detail as AccountCheckingDetailType).buffer)  }}</span>
            </div>
        </div>
    </div> 
</template>