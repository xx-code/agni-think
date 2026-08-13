<script setup lang="ts">
const { title, description, icon } = defineProps<{
    title: string
    description: string
    icon: string
}>()

const emit = defineEmits<{
    new: []
}>()

</script>

<template>
    <div class="empty-state">
        <UIcon :name="icon" class="empty-icon" />
        <h3 class="empty-title">{{ title }}</h3>
        <p class="empty-description">{{ description }}</p>
        <UButton 
            icon="i-lucide-plus" 
            label="Créer un Objectif" 
            size="xl"
            @click="emit('new')"
        />
    </div>
</template>

<style scoped lang="scss">
// Empty State
.empty-state {
    grid-column: 1 / -1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    text-align: center;
}

.empty-icon {
    width: 80px;
    height: 80px;
    color: #cbd5e1;
    margin-bottom: 1.5rem;
}

.empty-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: #475569;
    margin: 0 0 0.5rem 0;
}

.empty-description {
    font-size: 1rem;
    color: #94a3b8;
    margin: 0 0 2rem 0;
}
</style>