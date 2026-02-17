<script setup lang="ts">
import type { NuxtError } from '#app'
import { askToFinancePersonnalAdvisor, fetchLlmModels } from '~/composables/agents/chat'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  parts: [{ type: string; text: string }]
  timestamp?: Date
}

const { data: llmModels } = useAsyncData('llm-models', async () => {
  return await fetchLlmModels()
})

const selectedModel = ref()
const toast = useToast()
const status = ref<'ready' | 'error' | 'submitted'>('ready')
const messages = ref<ChatMessage[]>([])
const prompt = ref<string>('')
const messagesEndRef = ref<HTMLElement | null>(null)
const isThinking = ref(false)

const suggestedPrompts = [
  { icon: 'i-lucide-trending-up', label: 'Investment strategy', text: 'What investment strategy suits a moderate risk profile?' },
  { icon: 'i-lucide-shield-check', label: 'Risk assessment', text: 'How can I protect my portfolio against market volatility?' },
  { icon: 'i-lucide-pie-chart', label: 'Portfolio review', text: 'Review my asset allocation and suggest improvements' },
  { icon: 'i-lucide-globe', label: 'Market outlook', text: 'What are the key market trends to watch this year?' },
]

function scrollToBottom() {
  nextTick(() => {
    messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

async function ask(text?: string) {
  const inputText = text || prompt.value

  if (!selectedModel.value) {
    toast.add({ title: 'Select a model first', color: 'warning', icon: 'i-lucide-alert-triangle' })
    return
  }
  if (!inputText || inputText.trim() === '') return

  messages.value = [...messages.value, {
    id: Date.now().toString(),
    role: 'user',
    timestamp: new Date(),
    parts: [{ type: 'text', text: inputText }]
  }]

  status.value = 'submitted'
  isThinking.value = true
  scrollToBottom()

  try {
    const response = await askToFinancePersonnalAdvisor({
        model: selectedModel.value,
        question: prompt.value
    })
    prompt.value = ''

    messages.value = [...messages.value, {
        id: Date.now().toString(),
        role: 'assistant',
        timestamp: new Date(),
         parts: [{ type: 'text', text: response.message }]
    }]

    status.value = 'ready'
    isThinking.value = false
    scrollToBottom()
  } catch (err) {
    status.value = 'error'
    isThinking.value = false
    const nuxtError = err as NuxtError
    toast.add({ title: 'Error occurred', description: nuxtError.data as string, color: 'error' })
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    ask()
  }
}

function formatTime(date?: Date) {
  if (!date) return ''
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <!-- ── Toolbar ─────────────────────────────────────────── -->
    <div class="flex items-center justify-between flex-wrap gap-3">
      <!-- Identity -->
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-violet-100 flex items-center justify-center shadow-sm">
          <UIcon name="i-lucide-brain-circuit" class="text-violet-600 text-lg" />
        </div>
        <div>
          <p class="text-sm font-semibold text-gray-900 leading-none">Personal Advisor</p>
          <p class="text-xs text-gray-400 flex items-center gap-1.5 mt-1">
            <span
              class="inline-block w-1.5 h-1.5 rounded-full transition-colors"
              :class="isThinking ? 'bg-violet-400 animate-pulse' : 'bg-emerald-400'"
            />
            {{ isThinking ? 'Thinking…' : 'Ready to advise' }}
          </p>
        </div>
      </div>

      <!-- Controls -->
      <div class="flex items-center gap-2">
        <USelectMenu
          v-model="selectedModel"
          :items="llmModels"
          placeholder="Choose model"
          leading-icon="i-lucide-cpu"
          size="sm"
          class="min-w-44"
        />
        <UButton
          icon="i-lucide-rotate-ccw"
          variant="ghost"
          color="neutral"
          size="sm"
          :disabled="messages.length === 0"
          @click="messages = []"
        />
      </div>
    </div>

    <!-- ── Chat card ───────────────────────────────────────── -->
    <div class="rounded-2xl border border-gray-200 bg-white shadow-sm flex flex-col overflow-hidden">

      <!-- Messages scroll area — grows with content, max height before scrolling -->
      <div
        class="flex flex-col gap-6 p-6 overflow-y-auto"
        style="min-height: 400px; max-height: calc(100vh - 320px);"
      >

        <!-- ── Empty state ──────────────────────────────────── -->
        <Transition
          enter-active-class="transition-all duration-300 ease-out"
          enter-from-class="opacity-0 translate-y-2"
          leave-active-class="transition-all duration-150 ease-in"
          leave-to-class="opacity-0"
        >
          <div
            v-if="messages.length === 0 && !isThinking"
            class="flex flex-col items-center justify-center gap-10 h-full py-10"
          >
            <!-- Hero icon -->
            <div class="flex flex-col items-center gap-4 text-center">
              <div class="relative">
                <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-50 to-indigo-100 border border-violet-200 flex items-center justify-center shadow-inner">
                  <UIcon name="i-lucide-bot" class="text-violet-600 text-2xl" />
                </div>
                <span class="absolute -bottom-1.5 -right-1.5 w-4 h-4 bg-emerald-400 border-2 border-white rounded-full shadow" />
              </div>
              <div>
                <h2 class="text-base font-semibold text-gray-900">Your AI Financial Advisor</h2>
                <p class="text-sm text-gray-400 mt-1 max-w-xs leading-relaxed">
                  Ask me anything about investments, market trends, or portfolio strategy.
                </p>
              </div>
            </div>

            <!-- Suggestion cards -->
            <div class="grid grid-cols-2 gap-3 w-full max-w-lg">
              <button
                v-for="s in suggestedPrompts"
                :key="s.label"
                class="flex items-start gap-3 p-4 rounded-xl border border-gray-100 bg-gray-50 hover:bg-violet-50 hover:border-violet-200 transition-all duration-150 text-left group cursor-pointer"
                @click="ask(s.text)"
              >
                <div class="w-8 h-8 flex-shrink-0 rounded-lg bg-white border border-gray-200 group-hover:border-violet-200 flex items-center justify-center shadow-sm transition-colors duration-150">
                  <UIcon :name="s.icon" class="text-violet-500 text-sm" />
                </div>
                <div class="min-w-0">
                  <p class="text-xs font-semibold text-gray-700 mb-0.5 truncate">{{ s.label }}</p>
                  <p class="text-xs text-gray-400 leading-relaxed line-clamp-2">{{ s.text }}</p>
                </div>
              </button>
            </div>
          </div>
        </Transition>

        <!-- ── Messages ─────────────────────────────────────── -->
        <TransitionGroup
          tag="div"
          class="flex flex-col gap-5"
          enter-active-class="transition-all duration-250 ease-out"
          enter-from-class="opacity-0 translate-y-2"
        >
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="flex items-end gap-3"
            :class="msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'"
          >
            <!-- Avatar -->
            <div
              class="flex-shrink-0 w-8 h-8 rounded-xl flex items-center justify-center text-sm"
              :class="msg.role === 'user'
                ? 'bg-violet-100 text-violet-600'
                : 'bg-gray-100 text-gray-500'"
            >
              <UIcon :name="msg.role === 'user' ? 'i-lucide-user' : 'i-lucide-bot'" />
            </div>

            <!-- Bubble + timestamp -->
            <div
              class="flex flex-col gap-1 max-w-[72%]"
              :class="msg.role === 'user' ? 'items-end' : 'items-start'"
            >
              <div
                class="px-4 py-3 rounded-2xl text-sm leading-relaxed"
                :class="msg.role === 'user'
                  ? 'bg-violet-600 text-white rounded-br-sm shadow-sm'
                  : 'bg-gray-100 text-gray-800 rounded-bl-sm'"
              >
                {{ msg.parts[0].text }}
              </div>
              <span class="text-[10px] text-gray-300 px-1">{{ formatTime(msg.timestamp) }}</span>
            </div>
          </div>
        </TransitionGroup>

        <!-- ── Thinking dots ────────────────────────────────── -->
        <Transition
          enter-active-class="transition-all duration-200 ease-out"
          enter-from-class="opacity-0 translate-y-1"
          leave-active-class="transition-all duration-150 ease-in"
          leave-to-class="opacity-0"
        >
          <div v-if="isThinking" class="flex items-end gap-3">
            <div class="w-8 h-8 rounded-xl bg-gray-100 text-gray-400 flex items-center justify-center text-sm flex-shrink-0">
              <UIcon name="i-lucide-bot" />
            </div>
            <div class="px-4 py-3.5 rounded-2xl rounded-bl-sm bg-gray-100 flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full bg-gray-400 animate-bounce [animation-delay:0ms]" />
              <span class="w-2 h-2 rounded-full bg-gray-400 animate-bounce [animation-delay:150ms]" />
              <span class="w-2 h-2 rounded-full bg-gray-400 animate-bounce [animation-delay:300ms]" />
            </div>
          </div>
        </Transition>

        <!-- Scroll anchor -->
        <div ref="messagesEndRef" />
      </div>

      <!-- Divider -->
      <div class="border-t border-gray-100" />

      <!-- ── Input ────────────────────────────────────────────── -->
      <div class="p-4 bg-gray-50/70">
        <!-- Text area + actions -->
        <div
          class="flex items-end gap-2 bg-white rounded-xl border border-gray-200 px-4 py-3 shadow-sm transition-all duration-150 focus-within:border-violet-300 focus-within:ring-2 focus-within:ring-violet-100"
        >
          <UTextarea
            v-model="prompt"
            placeholder="Ask your advisor anything…"
            :rows="1"
            autoresize
            :maxrows="5"
            variant="none"
            class="flex-1 text-sm text-gray-800 bg-transparent resize-none focus:outline-none placeholder:text-gray-300"
            @keydown="handleKeydown"
          />
          <div class="flex items-center gap-1.5 flex-shrink-0 pb-0.5">
            <UButton
              icon="i-lucide-paperclip"
              variant="ghost"
              color="neutral"
              size="sm"
              class="text-gray-300 hover:text-gray-500"
            />
            <UButton
              icon="i-lucide-arrow-up"
              color="primary"
              size="sm"
              class="rounded-lg"
              :disabled="!prompt || prompt.trim() === '' || status === 'submitted'"
              :loading="status === 'submitted'"
              @click="ask()"
            />
          </div>
        </div>
      </div>
    </div>

  </div>
</template>