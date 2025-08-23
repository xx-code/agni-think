<script setup lang="ts">
const { title, targetAmount, amount} = defineProps<{
    title: string,
    targetAmount: number,
    amount: number 
}>()

const innerPercent = ref(computePercentage(targetAmount, amount))
const innerTarget = ref(targetAmount)

watch([() => targetAmount, () => amount], ([newTarget, newAmount]) => {
    innerPercent.value = computePercentage(newTarget, newAmount)
    innerTarget.value = newTarget
})

</script>

<template>
    <div class="bar-budget">
        <p>{{ title }}</p>
        <div class="flex justify-end text-sm">${{ innerTarget }}</div>
        <UProgress size="md" status v-model="innerPercent" />
    </div>
</template>
