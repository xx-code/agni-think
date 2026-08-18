<script setup lang="ts">
const { totalBalance, disponible, freeze, lock } = defineProps<{
    totalBalance: number
    disponible: number
    freeze: number
    lock: number
}>()
</script>

<template>
    <div class="flex flex-col gap-3 p-5">
        <div>
            <h6 class="text-gray-500 font-bold text-sm">Balance totale</h6>
            <h1 class="text-3xl font-semibold">{{ formatCurrency(totalBalance) }}</h1>
        </div>
        
        <div class="flex flex-wrap gap-2 font-semibold text-sm">
            <div class="flex items-center">
                <Icon name="i-lucide-circle-check" class="text-green-500" />
                <span class="ml-1">Disponible</span>
                <span class="ml-1">{{ formatCurrency(disponible) }}</span>
            </div>

            <div class="flex items-center">
                <Icon name="i-lucide-snowflake" class="text-blue-300" />
                <span class="ml-1">Gelé</span>
                <span class="ml-1">{{ formatCurrency(freeze) }}</span>
            </div>

            <div class="flex items-center">
                <Icon name="i-lucide-lock-keyhole" class="text-gray-500" />
                <span class="ml-1">Verrouillé</span>
                <span class="ml-1">{{ formatCurrency(lock) }}</span>
            </div>
        </div>
    </div>
</template>