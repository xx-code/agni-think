<script setup lang="ts">
const props = defineProps({
    id: String,
    title: String,
    balance: Number,
    freezedBalance: Number,
    lockedBalance: Number,
    diffPastBalancePer: Number,
    isPositif: Boolean,
    pastDateInfo: String,
    allowOpen: Boolean,
    allowEdit: Boolean,
    allowDelete: Boolean
})

const emit = defineEmits<{
    (e: 'edit', id?: string): void    
    (e: 'add', id?: string): void
    (e: 'delete', id?: string): void
}>(); 

</script>

<template>
    <div className="bg-white rounded-md p-2 border-1 border-gray-200">
        <div class="flex items-start justify-between">
            <div>
                <div className="text-xs text-gray-500">{{ title }}</div>
                <AmountTitle 
                    class="text-xl"
                    :amount="balance ?? 0"
                    :sign="'$'"
                />
                <div className="text-[11px] text-gray-400 mt-1">Freezed: ${{ freezedBalance?.toFixed(2) }} · Locked: ${{lockedBalance?.toFixed(2)}}</div>
            </div>
            <div className="text-right">
                <div className="mt-3 flex gap-2" />
                <!-- <UButton variant="outline" color="neutral" v-if="allowOpen" icon="i-lucide-external-link" @click="$emit('open', id)"/>           -->
                <UButton variant="outline" color="neutral" v-if="allowEdit" icon="i-lucide-pencil" @click="emit('edit', id)"/>          
                <UButton variant="outline" color="neutral" v-if="allowDelete" icon="i-lucide-trash-2" @click="$emit('delete', id)"/> 
            </div>
        </div> 

        <div class="flex justify-center">
            <UButton 
                size="xs"
                label="Ajoute"
                icon="i-lucide-banknote"
                variant="outline" 
                @click="$emit('add', id)"/>          
        </div>
    </div>
</template>

<style scoped lang="scss">
.card { 
    border: solid 1px #E6E6E6;
    padding: 0.5rem;
    // margin: 2px;
    // width: 235px;

    .card-title{
        .content {
            font-weight: bold;
            display: flex;
            justify-content: space-between;
            align-items: center;
            h2 {
                font-size: 1.15rem;
                color: #1E3050;
            }
        }
    }
    .card-content {
        .content {
            margin-top: 0.5rem;
            p {
                font-weight: bold;
                font-size: x-large;
                color: #1E3050;
            }
        }
    }
    .card-bottom {
        .content {
            margin-top: 0.5rem;
            display: flex;
            align-items: center;
            p {
                font-size: small;
                margin-left: 2.5px;
                color: #ADADAD;
            }
        }
    }
}

</style>