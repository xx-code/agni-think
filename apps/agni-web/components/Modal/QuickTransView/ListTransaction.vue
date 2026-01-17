<script setup lang="ts">
import type { SlideQuickViewTransactionType } from './index.vue';

const { transactions } = defineProps<{
    transactions: SlideQuickViewTransactionType[]
}>()

</script>

<template>
    <div>
        <div v-for="trans in transactions" :key="trans.id">

            <div class="flex items-center justify-between">
                <div class="flex gap-2">
                    <div class="flex items-center justify-center rounded-full"
                    :style="{ background: `${trans.color}22`, width: '35px',height: '35px'}">
                        <UIcon :name="trans.icon" class="text-white text-lg" :style="{color: trans.color }"/>  
                    </div>
                    <div>
                        <h4>{{ trans.category }}</h4>
                        <p class="text-xs text-gry-500">{{ trans.description }}</p>
                    </div>
                </div> 

                <div class="text-right">
                    <p class="text-sm">{{ formatDate(trans.date) }}</p>
                    <UKbd :value="trans.status" size="sm" />
                </div>
            </div> 

            <USeparator class="my-1" />
        </div>     
    </div>
</template>