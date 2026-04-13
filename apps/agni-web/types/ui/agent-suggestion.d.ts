export type AgentSuggestionStatusType = "Accepted" | "Rejected" | "Pending"
export type AgentSuggestionType = {
    agentId: string,
    agentName: string,
    title: string,
    description: string,
    confidenceScore: number,
    status: AgentSuggestionStatusType
}