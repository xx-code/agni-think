<script setup lang="ts">
const { 
    netWorthDates, 
    netWorthEvolutions,
} = defineProps<{
    netWorthDates: Date[]
    netWorthEvolutions: number[]
}>() 

const evolutionOptionChart = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    interaction: { intersect: false, mode: 'index' },
    plugins: {
        colors: { forceOverride: true },
        legend: { display: false },
        tooltip: {
            enabled: true,
            displayColors: false,
            callbacks: {
                label: (ctx: any) => formatCurrency(ctx.parsed.y),
                title: () => ''
            }
        }
    },
    scales: {
        y: { 
            display: false,
            // force le bas de l'échelle à démarrer proche du min des données
            // pour que le fill touche bien le bas du canvas
            min: Math.min(...netWorthEvolutions) * 0.9,
        },
        x: {
            display: true,
            grid: { display: false },
            border: { display: false },
            ticks: {
                autoSkip: false,
                color: '#94a3b8',
                font: { size: 11 }
            }
        }
    },
    elements: {
        line: { borderJoinStyle: 'round' }
    }
}))

const evolutionDataChart = computed(() => {
    const ctx = document.createElement('canvas').getContext('2d')!
    const gradient = ctx.createLinearGradient(0, 0, 0, 200)
    gradient.addColorStop(0, 'rgba(13, 148, 136, 0.35)')
    gradient.addColorStop(1, 'rgba(13, 148, 136, 0.02)') // reste légèrement visible en bas au lieu de 0
    return {
        labels: netWorthDates.map(i => formatDate(i)),
        datasets: [{
            data: netWorthEvolutions,
            borderColor: '#0d9488',
            borderWidth: 2.5,
            backgroundColor: gradient,
            fill: 'start',  // remplit jusqu'au bas de la zone visible, pas jusqu'au zéro absolu
            tension: 0,
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: '#0d9488',
            pointHitRadius: 10,
        }]
    }
})
</script>

<template>
    <div>
        <USeparator />
        <div class="h-40 mt-4">
            <LineChart :data="evolutionDataChart" :options="evolutionOptionChart" />
        </div>
    </div>
</template>