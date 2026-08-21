<script setup lang="ts">
const { 
    netWorthDates, 
    netWorthEvolutions,
    assetLabels,
    assetAmounts,
    liabilityAmounts,
    liabilityLabels
} = defineProps<{
    netWorthDates: Date[]
    netWorthEvolutions: number[]
    assetLabels: string[]
    assetAmounts: number[]
    liabilityLabels: string[]
    liabilityAmounts: number[]
}>()

const evolutionOptionChart = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    interaction: { intersect: false, mode: 'index' },
    plugins: {
        colors: { forceOverride: true},
        legend: { display: false },
        tooltip: {
            enabled: true,
            displayColors: false,
            callbacks: {
            label: (ctx:any) => formatCurrency(ctx.parsed.y),
            title: () => ''
            }
        }
    },
    scales: {
        y: { display: false, grace: '15%' },       // pas d'axe — c'est une sparkline
    },
    elements: {
      line: { borderJoinStyle: 'round' }
    } 
}))


const evolutionDataChart = computed(() => {
    const ctx = document.createElement('canvas').getContext('2d')!
    const gradient = ctx.createLinearGradient(0, 0, 0, 200)
    gradient.addColorStop(0, 'rgba(30, 41, 59, 0.15)')
    gradient.addColorStop(1, 'rgba(30, 41, 59, 0)')
    return {
        labels: netWorthDates.map(i => formatDate(i)),
        datasets: [{
            data: netWorthEvolutions,
            borderColor: '#4338ca',
            borderWidth: 2,
            backgroundColor: gradient,
            fill: true,
            tension: 0.4,
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: '#4338ca',
            pointHitRadius: 10,
        }]
    }
})

const repartitionOptionChart = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    cutout: '72%',              // donut plus fin et moderne (défaut ~50%)
    plugins: {
        legend: { display: false },   // on fait notre propre légende en HTML (voir plus bas)
        tooltip: {
            callbacks: {
                label: (ctx: any) => `${ctx.label}: ${formatCurrency(ctx.parsed)}`
            }
        }
    },
    elements: {
        arc: {
            borderWidth: 3,
            borderColor: '#ffffff',   // séparation nette entre segments
            hoverOffset: 8            // effet "pop" léger au survol
        }
    }
}))

const assetColors = computed(() => generateShades(155, assetLabels.length)) 
const liabilityColors = computed(() => generateShades(2, liabilityLabels.length))

const repartitionAssetDataChart = computed(() => ({
    labels: assetLabels,
    datasets: [{ data: assetAmounts, backgroundColor: assetColors.value, borderColor: '#fff', borderWidth: 3 }]
}))

const repartitionLiabilityDataChart = computed(() => ({
    labels: liabilityLabels,
    datasets: [{
        data: liabilityAmounts,
        backgroundColor: liabilityColors.value, borderColor: '#fff', borderWidth: 3
    }]
}))

</script>

<template>
    <div class="flex flex-col gap-5">
        <div class="bg-white p-5 rounded-2xl shadow-sm">
            <h2 class="text-sm text-gray-500 mb-1">Évolution du patrimoine</h2>
            <div class="h-40 mt-5">
                <LineChart :data="evolutionDataChart" :options="evolutionOptionChart" />
            </div>
        </div>

        <div class="grid md:grid-cols-2 grid-cols-1 gap-5 flex-wrap w-full">
            <div class="bg-white p-5 rounded-2xl shadow-sm">
                <div class="mb-4 ">
                    <h2 class="font-semibold text-xl">Actifs</h2>
                    <h6>
                        {{ 
                            formatCurrency(assetAmounts.reduce((prev, curr) => prev += curr, 0)) 
                        }}
                    </h6>
                </div> 
                <div class="h-40 max-w-40 mx-auto">
                    <DoughnutChart 
                        :data="repartitionAssetDataChart" 
                        :options="repartitionOptionChart"
                    />
                </div>
                <div class="flex flex-col gap-1.5 mt-4">
                    <div v-for="(label, i) in assetLabels" :key="label" class="flex items-center gap-2 text-sm text-gray-600">
                        <span class="w-2 h-2 rounded-full shrink-0" :style="{ background: assetColors[i] }" />
                        {{ label }} · {{ formatCurrency(assetAmounts[i]!) }}
                    </div>
                </div>
            </div> 

            <div class="bg-white p-5 rounded-2xl shadow-sm">
                <div class="mb-4 ">
                    <h2 class="font-semibold text-xl">Passifs</h2>
                    <h6>
                        {{ 
                            formatCurrency(liabilityAmounts.reduce((prev, curr) => prev += curr, 0)) 
                        }}
                    </h6>
                </div>
                
                <div class="h-40 max-w-40 mx-auto">
                    <DoughnutChart 
                        :data="repartitionLiabilityDataChart" 
                        :options="repartitionOptionChart"
                    />
                </div>
                
                <div class="flex flex-col gap-1.5 mt-4">
                    <div v-for="(label, i) in liabilityLabels" :key="label" class="flex items-center gap-2 text-sm text-gray-600">
                        <span class="w-2 h-2 rounded-full shrink-0" :style="{ background: liabilityColors[i] }" />
                        {{ label }} · {{ formatCurrency(liabilityAmounts[i]!) }}
                    </div>
                </div>
            </div> 
        </div> 
    </div>
</template>