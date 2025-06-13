<script setup lang="ts">
const props = defineProps({
    amount: Number,
})

const amountRounded = props.amount?.toFixed(2) ?? "0"
const integerValue = amountRounded.slice(0, amountRounded.indexOf(".")) 
const decimalValue = amountRounded.replace(integerValue + ".", "")

</script>

<template>
   <div class="amount-title text-2xl">
        <span class="integer">$</span>
        <div v-for="val in integerValue" :key="val">
            <span class="interger">{{ val }}</span>
            
        </div>
        <span class="interger">.</span>
        <div v-for="val in decimalValue" :key="val">
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