<script setup>
import { 
    Chart as ChartJS, Title, Tooltip, 
    Legend, BarElement, CategoryScale, 
    LinearScale, PointElement, LineElement
} from 'chart.js';
import { Line } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, LineElement, Title, Tooltip, Legend, PointElement)

const props = defineProps({
    data: Object,
    options: Object
})

</script>

<template>
    <Line :data="data" :options="options"/>
</template>

<style scoped>

</style>