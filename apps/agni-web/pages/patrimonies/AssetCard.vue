<script setup lang="ts">
    import type { PatrimonyType, TypePatrimony } from "~/types/ui/patrimony";

    const { patrimony } = defineProps<{
        patrimony: PatrimonyType
    }>()

    const emit = defineEmits<{
        (e: 'open', id: string): void    
        (e: 'update', id: string): void    
        (e: 'delete', id: string): void    
    }>()

    const computeDiff = (currentBalance: number, accountBalance: number, type: TypePatrimony) => {
    if (type === 'Asset')
        return currentBalance - accountBalance 
    return  accountBalance - currentBalance 
}

</script>

<template>
    <div 
        :class="['bg-white rounded-lg transition hover:shadow-md cursor-pointer px-2 py-2 card-asset',]"
        @click="emit('open', patrimony.id)">
        <div class="flex items-center justify-between gap-y-3">
            <h5 class="text-gray-500 text-sm">{{ patrimony.title }}</h5> 
            <UKbd 
                size="sm"
                :value="patrimony.type === 'Asset' ? 'Actif' : 'Passif'" 
                :class="['mt-2', patrimony.type =='Asset' ? 'bg-green-100 text-green-600 border-green-600' : 'bg-red-100 text-red-600']"
            />
        </div> 

        <p class="font-bold text-xl text-gray-900">
        {{ formatCurrency(patrimony.currentBalance) }}
        </p>

        <div class="flex justify-between items-center">
            <p 
                :class="[
                'text-sm',
                computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance, patrimony.type) > 0 
                    ? 'text-green-500' 
                    : 'text-red-500'
                ]">
                <span>
                {{ computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance, patrimony.type) > 0 ? '+' : '-' }}
                </span>
                {{
                Math.abs(
                    roundNumber(
                    computePercentage(
                        patrimony.currentBalance,
                        computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance, patrimony.type)
                    )
                    )
                )
                }}%
            </p>

            <div v-if="patrimony.id !== 'SAVE_GOAL'" class="flex gap-1 edit">
              <UButton icon="i-lucide-eye" variant="ghost" color="primary"  @click.stop="emit('open', patrimony.id)" />
              <UButton icon="i-lucide-pen" variant="ghost" color="info" @click.stop="emit('update', patrimony.id)" />
              <UButton icon="i-lucide-trash" variant="ghost" color="error" @click.stop="emit('delete', patrimony.id)" />
            </div>
          </div> 
    </div>   
</template>

<style lang="scss" scoped>
    .card-asset {
        .edit {
            opacity: 0;
        }
        :hover {
            .edit {
                opacity: 100;
            }
        }
    }
</style>