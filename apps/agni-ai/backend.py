import os
import requests

from qdrant_client import QdrantClient
from backend_dto import FinanceProfileResponse, BudgetResponse, SavingGoalResponse, \
    BankRegisterResponse, TagResponse, CategoryResponse, CreateInvoiceRequest, \
    ExternalTransactionRequest, CreatedResponse, ExternalTransactionResponse, DeductionResponse, \
    NotificationRequest, AnnualOutlookResponse, AccountResponse, InternalLoanResponse
from dotenv import load_dotenv
 
load_dotenv()

api_link = os.getenv("AGNI_API_LINK")
vector_db_link = os.getenv("QDRANT_LINK")

collection_name = os.getenv("QDRANT_INVOICE_COLLECTION_NAME")
collection_name = os.getenv("QDRANT_EXTERNAL_TRANSACTION_COLLECTION_NAME")

def push_notification_to_backend(input: NotificationRequest):
    res = requests.post(f"{api_link}/notifications", json=input.model_dump(mode="json"))
    res.raise_for_status()

def get_finance_profile() -> FinanceProfileResponse:
    response = requests.get(f"{api_link}/analytics/finance-profile")

    response.raise_for_status()

    data = response.json()
    return FinanceProfileResponse(**data) 

def query_rag(question: str, collection_name: str, limit: int = 10) -> str:
    qdrant = QdrantClient(url=vector_db_link)
    search_result = qdrant.query(collection_name=collection_name, query_text=question, limit=limit)

    return "\n".join([r.document for r in search_result])

def get_budgets() -> list[BudgetResponse]:
    response = requests.get(f"{api_link}/budgets?limit=0&offset=0&queryAll=true")

    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(BudgetResponse(**item))
    
    return res

def get_saving_goals() -> list[SavingGoalResponse]:
    response = requests.get(f"{api_link}/saving-goals?limit=0&offset=0&queryAll=true")

    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(SavingGoalResponse(**item))
    
    return res 

def get_internal_loans() -> list[SavingGoalResponse]:
    response = requests.get(f"{api_link}/internal-loans?limit=0&offset=0&queryAll=true")

    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(InternalLoanResponse(**item))
    
    return res 

def get_account_with_detail(id: str) -> list[AccountResponse]:
    response = requests.get(f"{api_link}/accounts/with-detail/{id}")
    response.raise_for_status()

    data = response.json()
    
    return AccountResponse(**data) 

def get_bank_registers() -> list[BankRegisterResponse]:
    response = requests.get(f"{api_link}/bank-registers/agent-level?limit=0&offset=0&queryAll=true")

    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(BankRegisterResponse(**item))
    
    return res

def get_bank_transaction_untreated() -> list[ExternalTransactionResponse]:
    response = requests.get(f"{api_link}/external-transactions?limit=0&offset=0&queryAll=true&isTreated=false")

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(ExternalTransactionResponse(**item))
    
    return res

def get_categories() -> list[CategoryResponse]:
    response = requests.get(f"{api_link}/categories?limit=0&offset=0&queryAll=true")

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(CategoryResponse(**item))
    
    return res

def get_tags() -> list[TagResponse]:
    response = requests.get(f"{api_link}/tags?limit=0&offset=0&queryAll=true&isSystem=false")

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(TagResponse(**item))
    
    return res

def get_annual_outlook() -> AnnualOutlookResponse:
    res = requests.get(f"{api_link}/analytics/annual-outlook")
    res.raise_for_status()
    result = res.json()

    return AnnualOutlookResponse(**result)

def get_deductions() -> list[DeductionResponse]:
    response = requests.get(f"{api_link}/deductions?limit=0&offset=0&queryAll=true")
    response.raise_for_status()

    data = response.json()
    items = data.get("items")

    res = []
    for item in items:
        res.append(DeductionResponse(**item))
    
    return res

def create_external_transaction(input: ExternalTransactionRequest) -> CreatedResponse:
    res = requests.post(f"{api_link}/external-transactions", json=input.model_dump(mode="json"))
    res.raise_for_status()
    result = res.json() 

    return CreatedResponse(**result)

def create_external_transactions(input: list[ExternalTransactionRequest]) -> list[CreatedResponse]:
    res = requests.post(f'{api_link}/external-transactions/many', json=[res.model_dump(mode="json") for res in input])
    res.raise_for_status()
    result = res.json() 

    if isinstance(result, list):
        return [CreatedResponse(**item) for item in result]
    
    return []

def create_transaction(input: CreateInvoiceRequest) -> CreatedResponse: 
    res = requests.post(f"{api_link}/invoices", json=input.model_dump(mode="json"))
    res.raise_for_status()
    result = res.json() 

    return CreatedResponse(**result)

def create_transactions(request: list[CreateInvoiceRequest]) -> list[CreatedResponse]: 
    pass


def update_cursor(bank_register_id: str, cursor: str):
    request = {
        "cursor": cursor
    }
    res = requests.put(f"{api_link}/bank-registers/{bank_register_id}", json=request)
    res.raise_for_status()

def treat_external_transaction(id: str):
    res = requests.post(f"{api_link}/external-transactions/treat/{id}")
    res.raise_for_status()