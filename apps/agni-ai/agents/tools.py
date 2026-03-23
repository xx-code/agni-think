import os

from dotenv import load_dotenv
from langchain.tools import tool
from typing import List  
from backend_dto import FinanceProfileResponse, BudgetResponse, SavingGoalResponse, AnnualOutlookResponse
from backend import get_finance_profile, query_rag, get_budgets, get_saving_goals, get_annual_outlook

load_dotenv()

invoice_collections=os.getenv("QDRANT_INVOICE_COLLECTION_NAME")
ext_transaction_collections=os.getenv("QDRANT_EXTERNAL_TRANSACTION_COLLECTION_NAME")

@tool
def wrap_tool_get_finance_profile() -> FinanceProfileResponse:
    """
    Retrieves a comprehensive financial overview. Use this to get data regarding 
    bank account balances, liquidity status, financial principles, upcoming 
    spend
    """
    return get_finance_profile()

@tool
def wrap_tool_query_rag_invoice(task: str, limit: int = 5) -> str:
    """
    Searches through stored invoice documents using a vector database. 
    Pass a specific natural language question to find details about past 
    transactions, vendors, or specific invoice items.
    """
    return query_rag(question=task, collection_name=invoice_collections, limit=limit)

@tool
def wrap_tool_query_rag_external_trans(task: str, limit: int = 5) -> str:
    """
    Searches through stored external transaction documents using a vector database. 
    Pass a specific natural language question to find details about past 
    transactions already treated by the system.
    """
    return query_rag(question=task, collection_name=ext_transaction_collections, limit=limit)

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

@tool
def wrap_tool_annual_outlook() -> AnnualOutlookResponse:
    """
    Récupère les projections financières annuelles complètes. 
    Fournit les revenus, dépenses et budgets prévisionnels vs actuels, 
    ainsi que la marge d'épargne (savingMargin). 
    Inclut également le détail des dépenses prévues et actuelles 
    ventilées par catégorie (UUID). 
    Utilise cet outil pour analyser la santé financière globale de l'année.
    """
    return get_annual_outlook()


