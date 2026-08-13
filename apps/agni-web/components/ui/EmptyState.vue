<script>
</script>

<template>
    <div v-if="!tableData?.length" class="empty-state">
        <UIcon name="i-lucide-target" class="empty-icon" />
        <h3 class="empty-title">Aucun objectif d'épargne</h3>
        <p class="empty-description">Commencez par créer votre premier objectif</p>
        <UButton 
            icon="i-lucide-plus" 
            label="Créer un Objectif" 
            size="xl"
            @click="openSavingGoal()"
        />
    </div>
</template>