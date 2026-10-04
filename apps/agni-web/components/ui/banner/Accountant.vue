<script setup lang="ts">
const { icon, title, amount, isMoney = true } = defineProps<{
    icon: { name: string, fontColor: string, backgroundColor: string}
    title: string
    amount: number
    isMoney?: boolean
}>()
</script>

<template>
    <div class="p-5 bg-white rounded-xl shadow-md border-solid border border-neutral-100 hover:shadow-lg transition-all ease delay-100">
        <div class="flex items-center gap-2 mb-4">
            <UIcon :name="icon.name" class="text-[40px] p-2" :style="{backgroundColor: icon.backgroundColor, color: icon.fontColor}" />
            <span class="font-medium text-[0.875rem] text-neutral-500">{{ title }}</span>
        </div>
        <div class="font-bold text-[2rem] tracking-tighter " :style="{color: icon.fontColor}">{{ isMoney ? formatCurrency(amount) : amount }}</div>
    </div>
</template>