<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<template>
  <div class="custom-card-title">
    <h3 class="title">{{ title }}</h3>
    <div class="actions">
      <slot />
    </div>
  </div>
</template>

<style scoped lang="scss">
.custom-card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #f1f3f5;
}

.title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.actions {
  display: flex;
  gap: 0.5rem;
}
</style>