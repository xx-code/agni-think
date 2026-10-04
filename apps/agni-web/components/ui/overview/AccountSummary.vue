<script setup lang="ts">
import type { TotalBalanceBufferIndicator } from '~/types/ui/overview';

const props = defineProps<{
    indicatorBalanceBuffer?: TotalBalanceBufferIndicator
    totalBalance: number
    disponible: number
    freeze: number
    lock: number
}>()

const bufferStyle = computed(() => {
    const levels = [props.indicatorBalanceBuffer?.level, props.indicatorBalanceBuffer?.estimateLevel]
    const level = levels.includes('error') ? 'error' : levels.includes('warning') ? 'warning' : 'success'

    switch (level) {
        case 'warning':
            return {
                text: 'text-orange-500',
                title: 'text-orange-600',
                progress: 'bg-orange-500',
                icon: 'i-lucide-triangle-alert',
                label: 'Buffer proche'
            }
        case 'error':
            return {
                text: 'text-red-500',
                title: 'text-red-600',
                progress: 'bg-red-500',
                icon: 'i-lucide-triangle-alert',
                label: 'Buffer enfreint'
            }
        default:
            return {
                text: 'text-green-500',
                title: 'text-green-600',
                progress: 'bg-green-500',
                icon: 'i-lucide-shield',
                label: 'Buffer respecté'
            }
    }
})

const isBufferAlert = computed(() => {
    const indicator = props.indicatorBalanceBuffer
    return indicator != undefined && (indicator.isUnderBuffer || indicator.estimateLevel !== 'success')
})

const bufferDifference = computed(() => {
    const diff = props.indicatorBalanceBuffer?.diffBalance ?? 0
    return `${diff >= 0 ? '+' : ''}${formatCurrency(diff)}`
})
</script>

<template>
    <div class="flex flex-col gap-3 p-5">
        <div>
            <div class="flex items-center gap-1.5">
                <h6 class="text-gray-500 font-bold text-sm">Balance totale</h6>

                <UPopover v-if="indicatorBalanceBuffer" :content="{ side: 'bottom' }">
                    <UIcon :name="bufferStyle.icon" class="w-4 h-4 cursor-help" :class="bufferStyle.text" />
                    <template #content>
                        <div class="max-w-64 space-y-1.5 bg-white text-gray-700 text-xs p-2.5 rounded-lg">
                            <p class="font-bold" :class="bufferStyle.title">{{ isBufferAlert ? 'Buffer à surveiller' : bufferStyle.label }}</p>
                            <p>{{ indicatorBalanceBuffer.description }}</p>
                            <p>
                                <span class="font-semibold">Solde actuel {{ formatCurrency(totalBalance) }}</span>
                                <span class="opacity-70"> · Buffer {{ formatCurrency(indicatorBalanceBuffer.buffer) }}</span>
                            </p>
                            <p class="font-semibold">{{ bufferDifference }}</p>
                            <UProgress
                                :model-value="indicatorBalanceBuffer.coverage"
                                :ui="{ indicator: indicatorBalanceBuffer.level === 'success' ? 'bg-green-500' : indicatorBalanceBuffer.level === 'warning' ? 'bg-orange-500' : 'bg-red-500' }"
                                size="xs"
                            />
                            <p class="font-semibold" :class="indicatorBalanceBuffer.estimateLevel === 'success' ? 'text-green-600' : indicatorBalanceBuffer.estimateLevel === 'warning' ? 'text-orange-600' : 'text-red-600'">
                                Solde estimé {{ formatCurrency(indicatorBalanceBuffer.projectedBuffer) }}
                            </p>
                            <p>
                                Marge estimée {{ `${indicatorBalanceBuffer.projectedBufferByBalance >= 0 ? '+' : ''}${formatCurrency(indicatorBalanceBuffer.projectedBufferByBalance)}` }}
                            </p>
                            <UProgress
                                :model-value="indicatorBalanceBuffer.estimateCoverage"
                                :ui="{ indicator: indicatorBalanceBuffer.estimateLevel === 'success' ? 'bg-green-500' : indicatorBalanceBuffer.estimateLevel === 'warning' ? 'bg-orange-500' : 'bg-red-500' }"
                                size="xs"
                            />
                        </div>
                    </template>
                </UPopover>
            </div>
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
