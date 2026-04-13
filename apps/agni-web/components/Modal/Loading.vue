<!-- components/LoadingModal.vue -->
<template>
  <div v-if="isLoading" class="overlay">
    <div class="modal">
      <div class="spinner"></div>
      <p>Loading...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
const { isLoading } = useLoading()
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999999;
}

.modal {
  background: white;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  min-width: 150px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #ddd;
  border-top: 4px solid black;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: auto;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>