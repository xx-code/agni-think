import os
from fastapi import FastAPI
from agents.tools import get_budgets, get_finance_profile, get_saving_goals, query_invoices
from agents.dto import ChatFinancialAdvisorAgentInput
from agents.chat import PersonalFinanceAdvisorAgent
from typing import List
from dotenv import load_dotenv
import requests

load_dotenv()

os.getenv("OPENAI_API_KEY")
ollama_link=os.getenv("OLLAMA_LINK")

app = FastAPI()

@app.get("/")
def read_root():
    return "Agni - Accounting Office Service"


@app.get("/models")
def list_models() -> List[str]:
    response = requests.get(f"{ollama_link}/api/tags") 
    data = response.json()
    models = data['models']
    result = [model['name'] for model in models]

    return result 

@app.post("/chat")
def chat_with_advisor(request: ChatFinancialAdvisorAgentInput) -> str:
    agent = PersonalFinanceAdvisorAgent(ollama_link=ollama_link)
    response = agent.ask(request)

    return response.message


@app.get("/test-tools")
def test_tool(tool: str):
    match tool.lower():
        case "budget":
            return get_budgets() 
        case "saving_goal":
            return get_saving_goals()
        case "vector_db":
            return query_invoices("spend high spend invoice")
        case "finance_profile":
            return get_finance_profile()
        case _:
            return "Unknow tool in the system"