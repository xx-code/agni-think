import type { ChatPersonnalFinanceAdvisorRequest, ChatPersonnalFinanceAdvisorResponse } from "~/types/api/agent";

export async function askToFinancePersonnalAdvisor(request: ChatPersonnalFinanceAdvisorRequest) : Promise<ChatPersonnalFinanceAdvisorResponse> {
    const res = await $fetch<string>('/api/agent-advisor/chat', {
        method: 'POST',
        body: request
    })

    return {
        message: res
    }
}

export async function fetchLlmModels(): Promise<string[]> {
    return await $fetch('/api/llm/models', {
        method: 'GET',
    })
}