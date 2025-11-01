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

</script>

<template>
    <div class="card rounded-md shrink-0">
        <div class="content">
            <div class="card-title">
                <div class="content">
                    <h2>{{ title }}</h2>
                    <div class="container-btn">
                        <UButton variant="outline" color="neutral" v-if="allowOpen" icon="i-lucide-external-link" @click="$emit('open', id)"/>          
                        <UButton variant="outline" color="neutral" v-if="allowEdit" icon="i-lucide-pencil" @click="$emit('edit', id)"/>          
                        <UButton variant="outline" color="neutral" v-if="allowDelete" icon="i-lucide-trash-2" @click="$emit('delete', id)"/>          
                    </div>
                </div>
            </div>

            <div @click="$emit('customClick', id)" style="cursor: pointer;">
                <!--- -->

                <div class="card-content">
                    <!-- <div class="content">
                        <p>${{ balance }}</p>
                    </div> -->
                    <AmountTitle 
                        :amount="balance ?? 0"
                        :sign="'$'"
                    />
                    <div class="text-xs text-gray-300">
                        <p>Freezed Balance: ${{ freezedBalance }}</p>
                        <p>Locked Balance: ${{ lockedBalance }}</p>
                    </div>
                </div>

                <!--- -->

                <div class="card-bottom">
                    <div class="content">
                        <div>
                            <UBadge 
                                variant="subtle"
                                :color="isPositif ? 'success' : 'error'"
                                :icon="isPositif ? 'i-lucide-arrow-up' : 'i-lucide-arrow-down'">
                                {{ diffPastBalancePer }}%
                            </UBadge>
                        </div>
                        <div>
                            <p>Vs {{ pastDateInfo }} precendent</p>
                        </div>
                    </div> 
                </div>

            </div>

            
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