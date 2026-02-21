import os
from backend_dto import FinanceProfileResponse, BudgetResponse, SavingGoalResponse
from langchain.tools import tool
from qdrant_client import QdrantClient
import requests
from typing import List
from dotenv import load_dotenv

load_dotenv()

api_link = os.getenv("AGNI_API_LINK")
vector_db_link = os.getenv("QDRANT_LINK")
collection_name = os.getenv("QDRANT_COLLECTION_NAME")

def get_finance_profile() -> FinanceProfileResponse:
    response = requests.get(f"{api_link}/analytics/finance-profile")

    response.raise_for_status()

    data = response.json()
    return FinanceProfileResponse(**data) 

def query_invoices(question: str) -> str:
    qdrant = QdrantClient(url=vector_db_link)
    search_result = qdrant.query(collection_name="agni", query_text=question, limit=10)

    return "\n".join([r.document for r in search_result])


def get_budgets() -> List[BudgetResponse]:
    response = requests.get(f"{api_link}/budgets?limit=0&offset=0&queryAll=true")

    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(BudgetResponse(**item))
    
    return res

def get_saving_goals() -> List[SavingGoalResponse]:
    response = requests.get(f"{api_link}/saving-goals?limit=0&offset=0&queryAll=true")

    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(SavingGoalResponse(**item))
    
    return res 
    

@tool
def wrap_tool_get_finance_profile() -> FinanceProfileResponse:
    """
    Retrieves a comprehensive financial overview. Use this to get data regarding 
    bank account balances, liquidity status, financial principles, upcoming 
    spend
    """
    return get_finance_profile()

@tool
def wrap_tool_query_invoices(question: str) -> str:
    """
    Searches through stored invoice documents using a vector database. 
    Pass a specific natural language question to find details about past 
    transactions, vendors, or specific invoice items.
    """
    return query_invoices(question=question)

@tool
def wrap_tool_get_budgets() -> List[BudgetResponse]:
    """
    Retrieves a list of all active budgets. This includes target amounts, 
    current balances, due dates, and links to associated saving goals.
    """
    return get_budgets()

@tool
def wrap_tool_get_saving_goals() -> List[SavingGoalResponse]:
    """
    Retrieves all specific saving goals. Use this to check progress on 
    long-term financial targets, descriptions of goals, and their 
    assigned importance or priority.
    """
    return get_saving_goals()


