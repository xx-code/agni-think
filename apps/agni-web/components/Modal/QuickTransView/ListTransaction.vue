<script setup lang="ts">
import type { SlideQuickViewTransactionType } from './index.vue';

const { transactions } = defineProps<{
    transactions: SlideQuickViewTransactionType[]
}>();

const expandedTransactions = ref<Set<string>>(new Set());

function toggleExpand(transactionId: string) {
    if (expandedTransactions.value.has(transactionId)) {
        expandedTransactions.value.delete(transactionId);
    } else {
        expandedTransactions.value.add(transactionId);
    }
}

function isExpanded(transactionId: string) {
    return expandedTransactions.value.has(transactionId);
}

function getStatusConfig(status: string) {
    const configs = {
        'Pending': { id: 'Wait', color: 'amber', label: 'En attente', icon: 'i-lucide-clock' },
        'Complete': { id: 'Done', color: 'green', label: 'Validé', icon: 'i-lucide-check-circle' },
        'Cancelled': { id: 'Cancel', color: 'red', label: 'Annulé', icon: 'i-lucide-x-circle' }
    };
    return configs[status as keyof typeof configs] || { color: 'gray', label: status, icon: 'i-lucide-circle' };
}

function getTypeColor(type: string) {
    return type !== 'Income' ? '#ef4444' : '#10b981';
}
</script>

<template>
    <div class="space-y-2">
        <div 
            v-for="trans in transactions" 
            :key="trans.id"
            class="group hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-lg transition-colors"
        >
            <!-- Transaction principale -->
            <div 
                class="flex items-start justify-between p-3 cursor-pointer"
                @click="trans.records && trans.records.length > 0 ? toggleExpand(trans.id) : null"
            >
                <!-- Gauche: Icône + Info -->
                <div class="flex gap-3 flex-1 min-w-0">
                    <!-- Chevron d'expansion -->
                    <div class="flex items-center">
                        <UButton
                            v-if="trans.records && trans.records.length > 0"
                            color="neutral"
                            variant="ghost"
                            icon="i-lucide-chevron-down"
                            size="xs"
                            square
                            :ui="{
                                leadingIcon: [
                                    'transition-transform duration-200',
                                    isExpanded(trans.id) ? 'rotate-180' : ''
                                ]
                            }"
                        />
                        <div v-else class="w-6"></div>
                    </div>

                    <!-- Icône principale (première catégorie ou icône de transaction) -->
                    <div 
                        class="flex items-center justify-center rounded-full flex-shrink-0"
                        :style="{ 
                            background: `${trans.color || trans.records?.[0]?.category.color}22`, 
                            width: '40px',
                            height: '40px'
                        }"
                    >
                        <UIcon 
                            :name="trans.icon || trans.records?.[0]?.category.icon || 'i-lucide-circle'" 
                            class="text-lg"
                            :style="{ color: trans.color || trans.records?.[0]?.category.color }"
                        />  
                    </div>

                    <!-- Détails -->
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2">
                            <h4 class="font-medium text-gray-900 dark:text-white truncate">
                                {{ trans.category || `${trans.records?.length || 0} article${(trans.records?.length || 0) > 1 ? 's' : ''}` }}
                            </h4>
                            <UBadge 
                                v-if="trans.records && trans.records.length > 1"
                                :label="`${trans.records.length}`"
                                color="info"
                                variant="subtle"
                                size="xs"
                            />
                        </div>
                        <p class="text-xs text-gray-500 dark:text-gray-400 truncate mt-0.5">
                            {{ trans.description }}
                        </p>
                        
                        <!-- Tags et budgets en preview -->
                        <div 
                            v-if="!isExpanded(trans.id) && trans.records && trans.records.length > 0"
                            class="flex flex-wrap gap-1 mt-1.5"
                        >
                            <UBadge
                                v-for="tag in trans.records[0]?.tags?.slice(0, 2)"
                                :key="tag.id"
                                :label="tag.value"
                                :style="{ borderColor: tag.color, color: tag.color }"
                                variant="outline"
                                size="xs"
                            />
                            <UBadge
                                v-if="trans.records[0]?.tags && trans.records[0].tags.length > 2"
                                :label="`+${trans.records[0].tags.length - 2}`"
                                color="info"
                                variant="subtle"
                                size="xs"
                            />
                        </div>
                    </div>
                </div>

                <!-- Droite: Date + Status + Total -->
                <div class="text-right flex-shrink-0 ml-3">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ formatDate(trans.date) }}
                    </p>
                    <div class="flex items-center justify-end gap-1 mt-1">
                        <UBadge 

                            :style="{ color: getStatusConfig(trans.status).color}"
                            :icon="getStatusConfig(trans.status).icon"
                            :label="getStatusConfig(trans.status).label"
                            :class="[ getStatusConfig(trans.status).id === 'Wait' ? 'text-amber-500 bg-amber-50 ring-amber-300' : '']"
                            variant="subtle"
                            size="xs"
                        />
                    </div>
                    <p 
                        class="text-base font-semibold mt-1"
                        :style="{ color: getTypeColor(trans.type) }"
                    >
                        {{ formatCurrency(trans.total) }}
                    </p>
                </div>
            </div>

            <!-- Section expandée avec les records -->
            <div 
                v-if="isExpanded(trans.id) && trans.records && trans.records.length > 0"
                class="px-3 pb-3 space-y-2"
            >
                <!-- Chaque record -->
                <div 
                    v-for="record in trans.records"
                    :key="record.id"
                    class="ml-9 pl-4 py-2 border-l-2 border-gray-200 dark:border-gray-700 space-y-2"
                >
                    <div class="flex items-start justify-between gap-3">
                        <!-- Info du record -->
                        <div class="flex items-start gap-2 flex-1 min-w-0">
                            <div 
                                class="flex items-center justify-center rounded-full flex-shrink-0"
                                :style="{ 
                                    background: `${record.category.color}22`, 
                                    width: '32px',
                                    height: '32px'
                                }"
                            >
                                <UIcon 
                                    :name="record.category.icon" 
                                    class="text-base"
                                    :style="{ color: record.category.color }"
                                />  
                            </div>

                            <div class="flex-1 min-w-0">
                                <p class="text-sm font-medium text-gray-900 dark:text-white truncate">
                                    {{ record.category.title }}
                                </p>
                                <p class="text-xs text-gray-500 dark:text-gray-400 truncate">
                                    {{ record.description }}
                                </p>

                                <!-- Tags et budgets du record -->
                                <div class="flex flex-wrap gap-1 mt-1.5">
                                    <UBadge
                                        v-for="tag in record.tags"
                                        :key="tag.id"
                                        :label="tag.value"
                                        :style="{ borderColor: tag.color, color: tag.color }"
                                        variant="outline"
                                        size="xs"
                                    />
                                    <UBadge
                                        v-for="budget in record.budgets"
                                        :key="budget.id"
                                        :label="budget.title"
                                        color="primary"
                                        variant="subtle"
                                        size="xs"
                                        icon="i-lucide-pie-chart"
                                    />
                                </div>
                            </div>
                        </div>

                        <!-- Montant du record -->
                        <p 
                            class="text-sm font-semibold flex-shrink-0"
                            :style="{ color: getTypeColor(record.type) }"
                        >
                            {{ formatCurrency(record.amount) }}
                        </p>
                    </div>
                </div>

                <!-- Déductions si présentes -->
                <div 
                    v-if="trans.deductions && trans.deductions.length > 0"
                    class="ml-9 mt-2 pt-2 border-t border-gray-200 dark:border-gray-700"
                >
                    <p class="text-xs font-medium text-gray-600 dark:text-gray-400 mb-1">
                        Déductions
                    </p>
                    <div class="space-y-1">
                        <div 
                            v-for="deduction in trans.deductions"
                            :key="deduction.name"
                            class="flex justify-between text-xs bg-amber-50 dark:bg-amber-900/20 rounded px-2 py-1"
                        >
                            <span class="text-gray-700 dark:text-gray-300">{{ deduction.name }}</span>
                            <span class="font-medium text-amber-700 dark:text-amber-400">
                                -{{ formatCurrency(deduction.amount) }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Résumé du total -->
                <div class="ml-9 mt-2 pt-2 border-t border-gray-200 dark:border-gray-700 flex justify-end">
                    <div class="space-y-0.5 text-xs">
                        <div class="flex justify-between gap-4">
                            <span class="text-gray-600 dark:text-gray-400">Sous-total:</span>
                            <span class="font-medium">{{ formatCurrency(trans.subTotal) }}</span>
                        </div>
                        <div 
                            v-if="trans.deductions && trans.deductions.length > 0"
                            class="flex justify-between gap-4"
                        >
                            <span class="text-gray-600 dark:text-gray-400">Déductions:</span>
                            <span class="font-medium text-amber-600">
                                -{{ formatCurrency(trans.deductions.reduce((s, d) => s + d.amount, 0)) }}
                            </span>
                        </div>
                        <div class="flex justify-between gap-4 text-sm font-bold pt-0.5 border-t">
                            <span>Total:</span>
                            <span :style="{ color: getTypeColor(trans.type) }">
                                {{ formatCurrency(trans.total) }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <USeparator class="my-1" />
        </div>     
    </div>
</template>

<style scoped>
.group:hover {
    @apply cursor-pointer;
}
</style>