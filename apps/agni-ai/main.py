import os
import time
from fastapi.concurrency import asynccontextmanager
import requests
import plaid

from datetime import date

from fastapi import FastAPI
from apscheduler.schedulers.background import BackgroundScheduler
from backend import get_budgets, get_finance_profile, get_saving_goals, query_rag, get_bank_registers, get_bank_transaction_untreated
from agents.dto import ChatFinancialAdvisorAgentInput
from agents.agent import Chat
from agents.tools import wrap_tool_annual_outlook, wrap_tool_get_budgets, wrap_tool_get_finance_profile, wrap_tool_query_rag_invoice

from storages.dto import QdrantAddDocumentInput
from storages.qdrant import QdrantClientService

from typing import Dict, List
from dotenv import load_dotenv

from frontend_dto import ExchangeTokenDto

from langchain_core.messages import BaseMessage

from plaid.api import plaid_api
from plaid.model.link_token_create_request import LinkTokenCreateRequest
from plaid.model.link_token_create_request_user import LinkTokenCreateRequestUser
from plaid.model.item_public_token_exchange_request import ItemPublicTokenExchangeRequest
from plaid.model.products import Products
from plaid.model.country_code import CountryCode

from interactions.plaid import loop_transactions, batch_fetch_transactions
from interactions.agent import treat_new_bank_transaction
from cachetools import TTLCache

load_dotenv()

os.getenv("OPENAI_API_KEY")
os.getenv("GOOGLE_API_KEY")

ollama_link=os.getenv("OLLAMA_LINK")
vector_db_link = os.getenv("QDRANT_LINK")
invoice_collections=os.getenv("QDRANT_INVOICE_COLLECTION_NAME")
env = os.getenv("ENV")
plaid_client_id = os.getenv("PLAID_API_CLIENT_ID")
plaid_secret = os.getenv("PLAID_API_SECRET")
plaid_rediction_url = os.getenv("PLAID_REDIRECT_URL")

plaid_host = plaid.Environment.Sandbox
if env.lower() == "production":
    plaid_host = plaid.Environment.Production

configuration = plaid.Configuration(
    host=plaid_host,
    api_key={
        'clientId': plaid_client_id,
        'secret': plaid_secret,
    }
)

api_client = plaid.ApiClient(configuration)
client = plaid_api.PlaidApi(api_client)


def sync_external_transaction() -> list[str]:
    newTransactionIds = []
    bank_registers = get_bank_registers()
    for bank in bank_registers:
        # TODO: Check if there are an error and desactive the register
        # Give the possibility to reconnect bank account
        newTransactionIds += loop_transactions(
            bank_register_id=bank.id, access_code=bank.accessCode, 
            init_cursor=bank.cursor, 
            isAllTreated=False)
    
    if len(newTransactionIds) == 0:
        return "No new Transaction"

    return newTransactionIds


scheduler = BackgroundScheduler()

@asynccontextmanager
async def lifespan(app: FastAPI):
    scheduler.add_job(sync_external_transaction, 'interval', hours=3)
    scheduler.add_job(treat_new_bank_transaction, 'cron', hour='0,12,18')
    scheduler.start()

    yield

    scheduler.shutdown()

app = FastAPI(lifespan=lifespan)
sessions_db = TTLCache(maxsize=100, ttl=86400)

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
    session_id = request.session_id 
    if session_id not in sessions_db:
        sessions_db[session_id] = []
    model = request.model

    if request.model == "":
        model = "google_genai:gemini-2.5-flash-lite" 

    chat = Chat(
        "./agents/knowledge/cfo.yaml", 
        model=model, 
        history=sessions_db[session_id], 
        tools=[wrap_tool_query_rag_invoice, wrap_tool_annual_outlook, wrap_tool_get_finance_profile, wrap_tool_get_budgets])
    response = chat.ask(request.question)

    return response

@app.post("/embedding-document")
def add_embedding_doc(request: QdrantAddDocumentInput):
    service = QdrantClientService(qdrant_url=vector_db_link) 
    service.add_embedding_text(request)

@app.delete("/embedding-documents/{collection_name}/{id}")
def remove_embedding_doc(collection_name: str, id: str):
    service = QdrantClientService(qdrant_url=vector_db_link) 
    service.remove_embedding_text(collection_name=collection_name, id=id)


@app.get("/test-tools")
def test_tool(tool: str):
    match tool.lower():
        case "budget":
            return get_budgets() 
        case "saving_goal":
            return get_saving_goals()
        case "vector_db":
            return query_rag(invoice_collections, "spend high spend invoice")
        case "finance_profile":
            return get_finance_profile()
        case _:
            return "Unknow tool in the system"

@app.post("/create-bank-link")
def create_link_token():
    request = LinkTokenCreateRequest(
        products=[Products("transactions")],
        client_name="Plaid Test App",
        country_codes=[CountryCode('US')],
        redirect_uri=plaid_rediction_url,
        language="en",
        user=LinkTokenCreateRequestUser(
            client_user_id=str(time.time())
        )
    )

    response = client.link_token_create(request)

    return response.to_dict()


@app.post('/exchange-public-token')
def exchange_public_token(request: ExchangeTokenDto):
    global access_token
    request = ItemPublicTokenExchangeRequest(
        public_token=request.public_token
    )
    response = client.item_public_token_exchange(request)

    access_token = response['access_token']
    # item_id = response['item_id']

    return {'code': access_token}

@app.get("/init-external-transactions")
def init_transactions(start_date: date | None = None):
    newTransactionIds = []
    bank_registers = get_bank_registers()
    if start_date is None:
        start_date = date(2023, 1, 1)
    for bank in bank_registers:
        newTransactionIds += batch_fetch_transactions(
            bank_register_id=bank.id, 
            access_code=bank.accessCode, 
            start_date=start_date, end_date=date.today())
    
    if len(newTransactionIds) == 0:
        return "No new Transaction"

    return newTransactionIds

@app.get("/force-sync-transaction")
def force_sync_transaction():
    sync_external_transaction()
    res = get_bank_transaction_untreated()
    if len(res) > 0:
        treat_new_bank_transaction()
 
@app.get("/health")
def health():
    # Check connection to ollama
    # Check connection to cloud ai
    # Check connection to vector db
    return "In services!!"