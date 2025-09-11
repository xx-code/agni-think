<script lang="ts" setup>
import useDeleteNotification from '~/composables/notifications/useDeleteNotificaiton';
import useNotifications from '~/composables/notifications/useNotifications'
import useToggleNotification from '~/composables/notifications/useToggleNotification'

const toast = useToast()
const emit = defineEmits<{ close: [boolean] }>()

// récupère toutes les notifications
const { data: notifications, refresh } = useNotifications({ 
  limit: 0, 
  offset: 0, 
  queryAll: true
})

async function toggleReadState(notification: any) {
  const oldState = notification.isRead
  // Optimistic update
  notification.isRead = !oldState
  try {
    await useToggleNotification(notification.id)
  } catch (err) {
    // rollback si erreur
    notification.isRead = oldState
    toast.add({
      title: 'Erreur',
      description: "Impossible de mettre à jour la notification : " + err,
      color: 'error'
    })
  }
}


async function deleteNotification(id: string) {
  try {
    // ton API delete
    await useDeleteNotification(id)
    await refresh()
  } catch (err) {
    toast.add({
      title: 'Erreur',
      description: 'Impossible de supprimer : ' + err,
      color: 'error'
    })
  }
}

</script>

<template>
    <USlideover :close="{ onClick: () => emit('close', false) }">
        <template #body>
            <div class="divide-y divide-gray-200">
                <div
                    v-for="notification in notifications?.items"
                    :key="notification.id"
                    class="flex items-start justify-between px-4 py-3 hover:bg-gray-50 cursor-pointer"
                    @click="toggleReadState(notification)"
                >
                    <!-- Zone gauche : indicateur + contenu -->
                    <div class="flex gap-3">
                    <!-- Point indicateur de lecture -->
                    <span
                        class="mt-2 h-2 w-2 rounded-full"
                        :class="notification.isRead ? 'bg-transparent' : 'bg-blue-500'"
                    ></span>

                    <div>
                        <h4
                        class="text-sm font-medium"
                        :class="notification.isRead ? 'text-gray-500' : 'text-gray-900'">
                        {{ notification.title }}
                        </h4>
                        <p
                        class="text-sm"
                        :class="notification.isRead ? 'text-gray-400' : 'text-gray-700'">
                        {{ notification.content }}
                        </p>
                        <p class="text-xs text-gray-400 mt-1">
                        {{
                            new Date(notification.dateTime).toLocaleDateString('fr-FR', {
                            day: 'numeric',
                            month: 'long',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                            })
                        }}
                        </p>
                    </div>
                    </div>

                    <!-- Bouton supprimer -->
                    <button
                        class="text-gray-400 hover:text-red-500 transition"
                        @click.stop="deleteNotification(notification.id)"
                        title="Supprimer">
                    ✕
                    </button>
                </div>
            </div>
        </template>
    </USlideover>
</template>