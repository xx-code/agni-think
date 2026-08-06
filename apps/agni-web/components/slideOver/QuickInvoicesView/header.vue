<script setup lang="ts">
const { accountName, currency, balance, gains, spend } = defineProps<{
    typeAccount: string,
    accountName: string,
    currency: string,
    balance: number,
    gains: number,
    spend: number
}>()

const emit = defineEmits<{
    createInvoice: [],
    transfer: [],
    freeze: []
}>()

</script>

<template>
    <div class="space-y-2">
        <div class="flex justify-between items-center">
            <h3 class="font-semibold text-neutral-500">{{ typeAccount }} · {{ accountName }}</h3>
            <h3>{{ currency }}</h3>
        </div>

        <div>
            <p class="font-semibold text-4xl">
                {{ roundNumber(balance) }}
            </p>
        </div>
        
        <div class="flex gap-3 p-1">
            <div class="flex-1 bg-neutral-50 p-2 rounded-lg">
                <h5 class="font-semibold text-gray-500 text-sm">Revenus</h5>
                <p class="text-green-600 font-medium">{{ formatCurrency(gains)  }}</p>
            </div>
            <div class="flex-1 bg-neutral-50 p-2 rounded-lg ">
                <h5 class="font-semibold text-gray-500 text-sm">Depense</h5>
                <p class="text-red-600 font-medium">{{ formatCurrency(spend) }}</p>
            </div>
        </div>

        <div class="flex gap-2 items-center p-1">
            <UButton 
                class="flex-1"
                label="Facture"
                icon="i-lucide-banknote"
                variant="soft"
                color="success"
                @click="emit('createInvoice')"
            />
            <UButton 
                class="flex-1"
                label="Tranfert"
                icon="i-lucide-arrow-left-right"
                variant="soft"
                color="info"
                @click="emit('transfer')"
            />
            <UButton 
                class="flex-1"
                label="Geler"
                icon="i-lucide-snowflake"
                variant="soft"
                color="neutral"
                @click="emit('freeze')"
            />
        </div>
    </div>
</template>