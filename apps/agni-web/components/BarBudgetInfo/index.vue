<script setup>
const props = defineProps({
    title: String,
    targetAmount: Number,
    amount: Number 
})
</script>

<template>
    <div class="bar-budget">
        <p>{{ title }}</p>
        <div class="flex justify-end text-sm">${{ targetAmount }}</div>
        <UProgress size="lg" v-model="props.amount" :max="props.targetAmount" />
    </div>
</template>

<style scoped lang="scss">
.bar-budget{
    p {
        
    }
}
</style>