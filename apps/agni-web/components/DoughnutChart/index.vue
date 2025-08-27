<script setup>
import { 
    Chart as ChartJS, Title, Tooltip, 
    Legend, CategoryScale, 
    LinearScale,
    ArcElement,
    Colors
} from 'chart.js';
import { Doughnut } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, ArcElement, Title, Tooltip, Legend, Colors)

const props = defineProps({
    data: Object,
    options: Object
})

</script>

<template>
    <Doughnut :data="data" :options="options"/>
</template>

<style scoped>

</style>