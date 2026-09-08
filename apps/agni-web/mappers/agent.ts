import type { ChatPersonnalFinanceAdvisorResponse } from "~/types/api/agent";
import type { ChatPersonnalFinanceAdvisor } from "~/types/ui/agent";

export function chatAgentMessageResponseToChatAgentMessage(data: ChatPersonnalFinanceAdvisorResponse): ChatPersonnalFinanceAdvisor {
    return data 
}