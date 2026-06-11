<script setup lang="ts">
import type { NuxtError } from '#app'
import { askToFinancePersonnalAdvisor, fetchLlmModels } from '~/composables/api/agents'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  parts: [{ type: string; text: string }]
  timestamp?: Date
}

// Logique existante préservée
const status = ref<'ready' | 'error' | 'submitted'>('ready')
const messages = ref<ChatMessage[]>([])
const prompt = ref<string>('')
const messagesEndRef = ref<HTMLElement | null>(null)
const isThinking = ref(false)
const sessionId = ref(generateUUID())

const scrollToBottom = () => {
  nextTick(() => {
    messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

async function ask(text?: string) {
  const inputText = text || prompt.value
  if (!inputText || inputText.trim() === '') return

  messages.value.push({
    id: Date.now().toString(),
    role: 'user',
    timestamp: new Date(),
    parts: [{ type: 'text', text: inputText }]
  })

  status.value = 'submitted'
  isThinking.value = true
  scrollToBottom()

  try {
    const response = await askToFinancePersonnalAdvisor({
      session_id: sessionId.value,
      model: "",
      question: inputText
    })
    prompt.value = ''
    messages.value.push({
      id: Date.now().toString(),
      role: 'assistant',
      timestamp: new Date(),
      parts: [{ type: 'text', text: response.message }]
    })
  } catch (err) {
    const nuxtError = err as NuxtError
    console.error(nuxtError)
  } finally {
    status.value = 'ready'
    isThinking.value = false
    scrollToBottom()
  }
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    ask()
  }
}
</script>

<template>
  <div class="h-full bg-white flex flex-col font-sans text-slate-900 overflow-hidden">
    
    <div class="flex-none pt-12 pb-8 flex flex-col items-center border-b border-slate-50 bg-white/80 backdrop-blur-md z-10">
      <div class="relative">
        <div v-if="isThinking" class="absolute inset-0 bg-violet-400/20 blur-2xl rounded-full animate-pulse scale-150"></div>
        
        <div class="relative w-24 h-24 bg-white rounded-full shadow-2xl border border-slate-100 flex items-center justify-center overflow-hidden transition-transform duration-500" :class="{'scale-110 rotate-3': isThinking}">
           <UIcon name="i-lucide-flame" class="text-5xl transition-colors duration-500" :class="isThinking ? 'text-orange-500' : 'text-slate-800'" />
        </div>
      </div>
      
      <div class="mt-4 text-center">
        <h1 class="text-lg font-bold tracking-tight">Agni CFO</h1>
        <p class="text-[10px] uppercase tracking-[0.2em] text-slate-400 font-semibold italic">Personal Finance Intelligence</p>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-6 py-10 scroll-smooth">
      <div class="max-w-2xl mx-auto space-y-12">
        
        <div v-for="msg in messages" :key="msg.id" class="animate-in fade-in slide-in-from-bottom-4 duration-700">
          
          <div v-if="msg.role === 'assistant'" class="flex flex-col items-start">
            <span class="text-[9px] font-black uppercase text-slate-300 mb-2 tracking-widest">CFO Transmission</span>
            <div class="relative bg-slate-50 border border-slate-100 p-6 rounded-2xl rounded-tl-none text-sm leading-relaxed shadow-sm">
              <p class="whitespace-pre-wrap">{{ msg.parts[0].text }}</p>
            </div>
          </div>

          <div v-else class="flex flex-col items-end">
            <div class="bg-white border border-slate-200 px-5 py-3 rounded-2xl rounded-br-none shadow-sm text-sm text-slate-600 font-medium italic">
              "{{ msg.parts[0].text }}"
            </div>
          </div>

        </div>

        <div v-if="isThinking" class="flex items-center gap-2 p-4 animate-pulse">
          <div class="w-1.5 h-1.5 bg-slate-300 rounded-full animate-bounce"></div>
          <div class="w-1.5 h-1.5 bg-slate-300 rounded-full animate-bounce [animation-delay:-0.15s]"></div>
          <div class="w-1.5 h-1.5 bg-slate-300 rounded-full animate-bounce [animation-delay:-0.3s]"></div>
        </div>

        <div ref="messagesEndRef" class="h-1" />
      </div>
    </div>

    <div class="flex-none p-8 bg-gradient-to-t from-white via-white to-transparent">
      <div class="max-w-2xl mx-auto relative group">
        <div class="absolute -inset-0.5 bg-gradient-to-r from-slate-100 to-slate-200 rounded-2xl blur opacity-25 group-focus-within:opacity-100 transition duration-500"></div>
        <div class="relative bg-white border border-slate-200 rounded-2xl flex items-end p-2 shadow-xl">
          <UTextarea
            v-model="prompt"
            placeholder="Consultez votre CFO..."
            autoresize
            :rows="1"
            :maxrows="4"
            variant="none"
            class="flex-1 text-sm py-3 px-4 font-medium"
            @keydown="handleKeydown"
          />
          <UButton
            icon="i-lucide-arrow-up"
            color="neutral"
            variant="solid"
            size="md"
            class="rounded-xl mb-1 mr-1"
            :loading="status === 'submitted'"
            @click="ask()"
          />
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* Scrollbar invisible pour garder le design "clean" */
::-webkit-scrollbar { display: none; }
* { -ms-overflow-style: none; scrollbar-width: none; }

.animate-in {
  animation-fill-mode: both;
}
</style>