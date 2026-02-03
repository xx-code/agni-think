<script setup lang="ts">
const props = defineProps<{
  amount: number
  sign?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  color?: 'default' | 'success' | 'error' | 'warning'
  showDecimals?: boolean
}>()

const sizeClasses = {
  sm: 'text-lg',
  md: 'text-2xl',
  lg: 'text-3xl',
  xl: 'text-4xl'
}

const colorClasses = {
  default: 'text-gray-900',
  success: 'text-green-600',
  error: 'text-red-600',
  warning: 'text-orange-600'
}

const formattedAmount = computed(() => {
  const value = Math.abs(props.amount)
  if (props.showDecimals ?? true) {
    return value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  })
})

const isNegative = computed(() => props.amount < 0)
</script>

<template>
  <span 
    class="amount-title"
    :class="[
      sizeClasses[size || 'md'],
      colorClasses[color || 'default'],
      { 'negative': isNegative }
    ]"
  >
    <span v-if="isNegative" class="amount-sign">-</span>
    <span v-if="sign" class="amount-currency">{{ sign }}</span>
    <span class="amount-value">{{ formattedAmount }}</span>
  </span>
</template>

<style scoped lang="scss">
.amount-title {
  font-weight: 700;
  letter-spacing: -0.025em;
  display: inline-flex;
  align-items: baseline;
  gap: 0.125rem;
  
  &.negative {
    color: #ef4444;
  }
}

.amount-currency {
  opacity: 0.8;
}

.amount-sign {
  margin-right: 0.125rem;
}
</style>