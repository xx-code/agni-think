<script lang="ts" setup>
const { currentSize, totalData, hasMore, maxToDisplay, loading } = defineProps<{
    currentSize: number
    totalData: number
    hasMore: boolean
    maxToDisplay: number
    loading: boolean
}>()

const emit = defineEmits<{
    showMore: []
}>()

const progressPercent = computed(() => {
    if (totalData === 0) return 0
    return Math.min(100, Math.round((currentSize / totalData) * 100))
})

</script>

<template>
    <div class="py-6">
        <!-- Limite d'affichage atteinte, mais il reste des articles au-delà -->
        <div
            v-if="hasMore && currentSize >= maxToDisplay"
            class="flex flex-col items-center gap-3 max-w-sm mx-auto"
        >
            <div class="w-full">
                <div class="flex justify-between text-xs text-neutral-400 mb-1.5">
                    <span>{{ currentSize }} sur {{ totalData }} articles</span>
                    <span>{{ progressPercent }}%</span>
                </div>
                <div class="h-1.5 w-full rounded-full bg-neutral-100 dark:bg-neutral-800 overflow-hidden">
                    <div
                        class="h-full rounded-full bg-primary-500 transition-all duration-300"
                        :style="{ width: `${progressPercent}%` }"
                    />
                </div>
            </div>

            <p class="text-sm text-neutral-400 text-center">
                Affichage limité à {{ maxToDisplay }} articles à la fois.
                Continuez ou affinez avec un filtre pour aller plus vite.
            </p>

            <UButton
                label="Afficher plus"
                variant="outline"
                color="neutral"
                icon="i-lucide-chevron-down"
                :loading="loading"
                :disabled="loading"
                @click="emit('showMore')"
            />
        </div>

        <!-- Fin de liste -->
        <div v-else-if="!hasMore" class="flex flex-col items-center gap-1.5">
            <UIcon name="i-lucide-check-circle-2" class="text-lg text-emerald-500" />
            <p class="text-sm text-neutral-400 text-center">
                Vous avez atteint la fin de la liste 
                <span class="font-semibold text-neutral-600 dark:text-neutral-300">{{ totalData }} articles</span>
                au total.
            </p>
        </div>
    </div>
</template>