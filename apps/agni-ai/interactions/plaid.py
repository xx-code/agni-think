import os

from dotenv import load_dotenv
import plaid
import json

from dataclasses import dataclass
from datetime import date
from time import sleep

from plaid.api import plaid_api
from plaid.model.transactions_sync_request import TransactionsSyncRequest
from plaid.model.transactions_get_request import TransactionsGetRequest
from plaid.model.transactions_get_request_options import TransactionsGetRequestOptions
from backend import update_cursor, create_external_transaction, create_external_transactions 
from backend_dto import ExternalTransactionRequest

load_dotenv()

env = os.getenv("ENV")
plaid_client_id = os.getenv("PLAID_API_CLIENT_ID")
plaid_secret = os.getenv("PLAID_API_SECRET")

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

@dataclass
class ReadTransactionModel:
    next_cursor: str
    added: list
    modified: list
    removed: list
    has_more: bool


def fetch_transactions(access_token: str, start_date: date, end_date: date) -> list[any]:
    try:
        request = TransactionsGetRequest(
            access_token=access_token,
            start_date=start_date,
            end_date=end_date
        )
        response = client.transactions_get(request)
        transactions = response['transactions']

        while len(transactions) < response['total_transactions']:
            request = TransactionsGetRequest(
                access_token=access_token,
                start_date=start_date,
                end_date=end_date,
                options=TransactionsGetRequestOptions(
                    offset=len(transactions)
                )
            )
            response = client.transactions_get(request)
            transactions.extend(response['transactions'])
        
        return transactions 

    except plaid.ApiAttributeError as e:
        response = json.loads(e.body)
        error_fmt = dict(
            {'error': {'status_code': e.status, 'display_message':
            response['error_message'], 
            'error_code': response['error_code'], 'error_type': response['error_type']}})
        
        raise error_fmt

def read_transaction(cursor: str, access_token: str, count:int=100) -> ReadTransactionModel:
    try:
        request = TransactionsSyncRequest(
            access_token=access_token,
            cursor=cursor,
            count=count
        )
        response = client.transactions_sync(request).to_dict()
        
        added = response['added']
        modified = response['modified']
        removed = response['removed']
        has_more = response['has_more']

        return ReadTransactionModel(response['next_cursor'], added, modified, removed, has_more)
    except plaid.ApiException as e:
        response = json.loads(e.body)
        error_fmt = dict(
            {'error': {'status_code': e.status, 'display_message':
            response['error_message'], 
            'error_code': response['error_code'], 'error_type': response['error_type']}})
        
        raise error_fmt
    
    
def loop_transactions(bank_register_id: str, access_code: str, init_cursor: str = '', isAllTreated: bool = False, iteration: int | None = None) -> list[str]:
    cursor = init_cursor
    transactions = []
    seen_ids = set()
    has_more = True
    iteration_count = 0
    MAX_TRY = 10000

    seen_ids = set()

    while has_more and iteration_count < MAX_TRY:
        res = read_transaction(cursor=cursor, access_token=access_code)

        for trans in res.added:
            tid = trans.get("transaction_id")
            if tid in seen_ids:
                continue
            seen_ids.add(tid)
            transactions.append(trans)

        cursor = res.next_cursor
        has_more = res.has_more
        iteration_count += 1

        print(f"[DEBUG] iteration={iteration_count}, cursor={cursor}, has_more={res.has_more}, added={len(res.added)}")

    created_ids = []
    ext_transactions = []
    for trans in transactions:
        category = trans.get("personal_finance_category") or {}
        ext_transaction = ExternalTransactionRequest(
            accountId=trans.get("account_id"),
            transactionId=trans.get("transaction_id"),
            amount=trans.get("amount", 0),
            dateTransaction=trans.get("date"),
            merchantName=trans.get("merchant_name", ""),
            categoryPrimary=category.get("primary", ""),
            categoryDetail=category.get("detailed", ""),
            isTreated=isAllTreated
        )
        ext_transactions.append(ext_transaction)

        

    try:
        res = create_external_transactions(ext_transactions)
        created_ids = [x.newId for x in res]  
        update_cursor(bank_register_id=bank_register_id, cursor=cursor)
        return created_ids
    except Exception as e:
        print(f"Failed transaction: {e}")
    
    return []

    
 


def update_position_cursor(access_code: str, init_cursor: str = "") -> str: 
    cursor = init_cursor
    has_more = True

    while has_more:
        try:
            res = read_transaction(cursor=cursor, access_token=access_code, count=500)
            
            cursor = res.next_cursor
            has_more = res.has_more

            if has_more:
                sleep(0.1) 

        except Exception as e:
            print(f"Erreur lors du sync: {e}")
            return cursor
    
    return cursor
        


def batch_fetch_transactions(bank_register_id: str, access_code: str, start_date: date, end_date: date) -> list[str]:
    transactions = fetch_transactions(access_token=access_code, start_date=start_date, end_date=end_date)
    if len(transactions) > 0:
        next_cursor = update_position_cursor(access_code=access_code)
        update_cursor(bank_register_id=bank_register_id, cursor=next_cursor)
        external_transaction_fmt = [
            ExternalTransactionRequest(
                accountId=trans.get("account_id"),
                amount=trans.get("amount", 0),
                dateTransaction=trans.get("date"),
                # Fallback to "name" if "merchant_name" is null
                merchantName=trans.get("merchant_name") or trans.get("name") or "Unknown",
                # Safely access nested category fields
                categoryPrimary=(trans.get("personal_finance_category") or {}).get("primary", "UNCATEGORIZED"),
                categoryDetail=(trans.get("personal_finance_category") or {}).get("primary", "UNCATEGORIZED"),
                isTreated=True
            ) 
            for trans in transactions
        ]

        res = create_external_transactions(external_transaction_fmt)
        newIds = list(map(lambda x: x.newId, res)) 
        return newIds

    return []

# def dd():
#     cursor = ''

#     added = []
#     modified = []
#     removed = []
#     has_more = True


#     try:
#         while has_more:
#             request = TransactionsSyncRequest(
#                 access_token=access_token,
#                 cursor=cursor
#             )
#             response = client.transactions_sync(request).to_dict()

#             if cursor == '':
#                 time.sleep(2)
#                 continue
            
#             added.extend(response['added'])
#             modified.extend(response['modified'])
#             removed.extend(response['remove'])
#             has_more = response['has_more']

#         latest_transactions = sorted(added, key=lambda t: t['date'])[-8:]
#         return { 'latest_transactions': latest_transactions}
#     except plaid.ApiException as e:
#       response = json.loads(e.body)
#       return dict(
#           {'error': {'status_code': e.status, 'display_message':
#         response['error_message'], 'error_code': response['error_code'], 'error_type': response['error_type']}})