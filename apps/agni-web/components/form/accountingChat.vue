<script lang="ts" setup>
const { messages, isLoading, sendMessage, reset } = useAccountingChat()
const draft = ref('')
const scrollContainer = ref<HTMLElement | null>(null)

const suggestions = [
    'Quel est mon solde de trésorerie actuel ?',
    'Fais-moi un résumé des dépenses du mois',
    'Est-ce que je peux me permettre cet achat ?'
]

async function handleSend(text?: string) {
    const question = text ?? draft.value
    if (!question.trim()) return
    draft.value = ''
    await sendMessage(question)
    await nextTick()
    scrollToBottom()
}

function scrollToBottom() {
    if (scrollContainer.value) {
        scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
    }
}

function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        handleSend()
    }
}

watch(messages, () => nextTick(scrollToBottom), { deep: true })
</script>

<template>
    <div class="flex h-120 w-96 flex-col overflow-hidden rounded-lg">
        <div class="flex items-center gap-2 border-b border-gray-200 px-4 py-3 dark:border-gray-800">
            <div class="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900">
                <UIcon name="i-lucide-bot" class="h-4 w-4 text-primary-600 dark:text-primary-400" />
            </div>
            <div class="flex-1">
                <p class="text-sm font-semibold text-gray-900 dark:text-white">Agent CFO</p>
                <p class="text-xs text-gray-500">Assistant financier</p>
            </div>
            <UButton
                v-if="messages.length"
                variant="ghost"
                color="neutral"
                size="xs"
                icon="i-lucide-rotate-ccw"
                aria-label="Nouvelle conversation"
                @click="reset"
            />
        </div>

        <div ref="scrollContainer" class="flex-1 space-y-3 overflow-y-auto px-4 py-3">
            <div v-if="!messages.length" class="flex h-full flex-col items-center justify-center gap-3 text-center">
                <UIcon name="i-lucide-sparkles" class="h-6 w-6 text-gray-400" />
                <p class="text-sm text-gray-500">Pose une question sur tes finances</p>
                <div class="flex w-full flex-col gap-1.5">
                    <UButton
                        v-for="s in suggestions"
                        :key="s"
                        variant="soft"
                        color="neutral"
                        size="xs"
                        class="justify-start"
                        @click="handleSend(s)"
                    >
                        {{ s }}
                    </UButton>
                </div>
            </div>

            <div
                v-for="msg in messages"
                :key="msg.id"
                class="flex"
                :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
            >
                <div
                    class="max-w-[80%] rounded-lg px-3 py-2 text-sm"
                    :class="msg.role === 'user'
                        ? 'bg-primary-500 text-white'
                        : msg.status === 'error'
                            ? 'bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300'
                            : 'bg-gray-100 text-gray-900 dark:bg-gray-800 dark:text-gray-100'"
                >
                    <template v-if="msg.status === 'sending'">
                        <span class="flex gap-1">
                            <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-current [animation-delay:-0.3s]" />
                            <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-current [animation-delay:-0.15s]" />
                            <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-current" />
                        </span>
                    </template>
                    <template v-else>
                        {{ msg.content }}
                    </template>
                </div>
            </div>
        </div>

        <div class="flex items-end gap-2 border-t border-gray-200 p-2 dark:border-gray-800">
            <UTextarea
                v-model="draft"
                :rows="1"
                autoresize
                :maxrows="4"
                placeholder="Écris ton message..."
                class="flex-1"
                :disabled="isLoading"
                @keydown="onKeydown"
            />
            <UButton
                icon="i-lucide-send"
                :loading="isLoading"
                :disabled="!draft.trim() || isLoading"
                aria-label="Envoyer"
                @click="handleSend()"
            />
        </div>
    </div>
</template>