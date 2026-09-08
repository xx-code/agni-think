from datetime import date
import os

from dotenv import load_dotenv
from langchain.tools import tool
from typing import List  
from backend_dto import FinanceProfileResponse, BudgetResponse, SavingGoalResponse, AnnualOutlookResponse, AccountResponse,InternalLoanResponse, ForcastSpendingResponse, ForcastSpendingRequest
from backend import get_finance_profile, query_rag, get_budgets, get_saving_goals, get_annual_outlook, \
    get_account_with_detail, get_internal_loans, forcast_spending

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


@tool
def wrap_tool_get_account_by_id(id: str) -> AccountResponse:
    """
    Recupere les information sur un compte avec son ID (UUID)
    """
    return get_account_with_detail(id)


@tool
def wrap_tool_get_internal_loans() -> InternalLoanResponse:
    """
    Récupère la liste exhaustive des prêts internes (auto-endettement) actifs et passés.
    
    CE QUE CE TOOL PERMET À L'AGENT :
    1. Calculer le 'Total Collateral Locked' : Somme des montants d'épargne rendus 
       indisponibles par ces prêts.
    2. Vérifier le 'Principle Compliance' : S'assurer que le total des prêts ne dépasse 
       pas 10% de l'épargne liquide (Savings).
    3. Analyser la 'Liquidity Gap' : Comparer les dates d'échéance (due_date) des prêts 
       avec les revenus entrants (Coming Revenue).
    
    Indispensable pour Agni_Controller (Audit de risque) et Agni_Treasurer (Optimisation cash-flow).
    """
    return get_internal_loans()

@tool
def wrap_tool_forcast_spending(start_date: str, end_date: str) -> ForcastSpendingResponse:
    """Calcule et prévoit les projections financières (revenus, dépenses, épargne) sur une période donnée.

    Cette fonction analyse la situation financière prévisionnelle entre deux dates en combinant 
    le solde actuel des comptes, les échéances de factures/revenus planifiés (ScheduleInvoices), 
    les dépenses budgétisées et la cible d'épargne définie. Elle effectue également une évaluation 
    récursive des envies d'achats (wantItems) pour déterminer quels articles peuvent être approuvés.

    Args:
        startDate (date): Date de début de la période de prévision.
        endDate (date): Date de fin de la période de prévision.

    Returns:
        ForcastSpendingResponse: Un objet contenant le détail des prévisions financières :
            - remainAmount (float): Solde restant disponible après toutes les dépenses et l'épargne.
            - totalExpectedIncome (float): Total des revenus prévus (revenus planifiés + solde initial + déblocages).
            - totalExpectedExpense (float): Total des charges prévues (fixes, variables, budgets, gelées et épargne).
            - expectedIncome (float): Montant total des revenus récurrents planifiés sur la période.
            - expectedFixExpense (float): Montant total des charges fixes planifiées.
            - expectedVariableExpense (float): Montant total des charges variables planifiées.
            - expectedPlanFreezeExpense (float): Dépenses liées aux échéances gelées ou bloquées.
            - expectedBudgetExpense (float): Ajustement des budgets récurrents sur la période.
            - expectedSaving (float): Montant calculé mis de côté pour l'épargne.
            - itemsApproved (List[WantItemResponse]): Liste des éléments d'achat optionnels ("wants") validés par le calcul.
            - itemsRejected (List[WantItemResponse]): Liste des éléments d'achat optionnels refusés par manque de budget.
    """
    parsed_start = date.fromisoformat(start_date)
    parsed_end = date.fromisoformat(end_date)

    return forcast_spending(
        ForcastSpendingRequest(
            startDate=parsed_start,
            endDate=parsed_end,
            budgetIds=[],
            overrideAccountsBalance=None,
            savingAdditionalIncome=[],
            savingRate=None,
            wantItems=[]
        )
    )