from pydantic import BaseModel

class ChatFinancialAdvisorAgentInput(BaseModel):
    session_id: str
    model: str
    question: str

class ChatAgentOutput(BaseModel):
    message: str

class AgentSuggestionOutput(BaseModel):
    agentId: str
    agentName: str
    title: str
    description: str
    isHaveToBeReviewByHuman: bool
    confidenceScore: float