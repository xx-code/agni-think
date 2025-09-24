<script setup lang="ts">
const {amount, sign = "$"} = defineProps<{ amount: number, sign: string }>()


const amountRounded = ref<string>(amount?.toFixed(2) ?? "0") 
const amountValue = computed<{integer: string, decimal: string}>(() => { 
    let integerValue = amountRounded.value.slice(0, amountRounded.value.indexOf(".")) 
    return {
        integer: integerValue,
        decimal: amountRounded.value.replace(integerValue + ".", "")
    }
}) 

watch([() => amount], () => {
    amountRounded.value = amount.toFixed(2) 
})


</script>

<template>
   <div class="amount-title text-2xl">
        <span class="integer">{{sign}}</span>
        <div v-for="val in amountValue.integer" :key="val">
            <span class="interger">{{ val }}</span>
            
        </div>
        <span class="interger">.</span>
        <div v-for="val in amountValue.decimal" :key="val">
            <span class="decimal">{{ val }}</span>
        </div>
   </div> 
</template>

<style lang="scss" scoped>
.amount-title {
    display: flex;
    font-weight: bold;
    .interger {
        color: #1E3050;
    } 

    .decimal {
        color: #D9D9D9;
    }
}
</style>