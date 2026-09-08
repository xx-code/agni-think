import { chatAgentMessageResponseToChatAgentMessage } from '~/mappers/agent'
import { API_ROUTES } from '~/shared/routes'
import type { ChatPersonnalFinanceAdvisorRequest, ChatPersonnalFinanceAdvisorResponse } from '~/types/api/agent'

export type ChatRole = 'user' | 'assistant'

export type ChatMessage = {
    id: string
    role: ChatRole
    content: string
    status?: 'sending' | 'error'
}

export function useAccountingChat() {
    const sessionId = useState('accounting-chat-session-id', () => crypto.randomUUID())
    const messages = useState<ChatMessage[]>('accounting-chat-messages', () => [])
    const isLoading = useState('accounting-chat-loading', () => false)

    async function sendMessage(question: string) {
        const trimmed = question.trim()
        if (!trimmed || isLoading.value) return

        const pendingId = crypto.randomUUID()
        messages.value.push({ id: crypto.randomUUID(), role: 'user', content: trimmed })
        messages.value.push({ id: pendingId, role: 'assistant', content: '', status: 'sending' })
        isLoading.value = true

        try {
            const res = await ApiLinkBuilder
                .route<ChatPersonnalFinanceAdvisorResponse>(API_ROUTES.AGENTS.CHAT)
                .mapper(chatAgentMessageResponseToChatAgentMessage)
                .body({
                    session_id: sessionId.value,
                    model: 'default',
                    question: trimmed
                } satisfies ChatPersonnalFinanceAdvisorRequest)
                .execute()

            const index = messages.value.findIndex(m => m.id === pendingId)
            if (index >= 0) {
                messages.value.splice(index, 1, { id: pendingId, role: 'assistant', content: res.message })
            }
        } catch {
            const index = messages.value.findIndex(m => m.id === pendingId)
            if (index >= 0) {
                messages.value.splice(index, 1, {
                    id: pendingId,
                    role: 'assistant',
                    content: "Désolé, une erreur est survenue. Réessaie dans un instant.",
                    status: 'error'
                })
            }
        } finally {
            isLoading.value = false
        }
    }

    function reset() {
        messages.value = []
        sessionId.value = crypto.randomUUID()
    }

    return { messages, isLoading, sendMessage, reset }
}