<script setup>
import { 
    Chart as ChartJS, Title, Tooltip, 
    Legend, BarElement, CategoryScale, 
    LinearScale
} from 'chart.js';
import { Bar} from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps({
    data: Object,
    options: Object
})

</script>

<template>
    <Bar :data="data" :options="options"/>
</template>

<style scoped>

</style>