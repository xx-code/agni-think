from typing import List, Optional
from pydantic import BaseModel

class ChatFinancialAdvisorAgentInput(BaseModel):
    model: str
    question: str

class ChatAgentOutput(BaseModel):
    message: str