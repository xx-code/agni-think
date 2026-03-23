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

configuration = plaid.Configuration(
    host=plaid.Environment.Sandbox,
    api_key={
        'clientId': "66b5e48df271e2001a12e769",
        'secret': "de350503ed97d8da5f3224a85cc386",
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

    added = []
    has_more = True
    count = 1

    while has_more:
        res = read_transaction(cursor=cursor, access_token=access_code)

        added.extend(res.added)
        cursor = res.next_cursor
        if iteration is not None and count == iteration:
            has_more = False
        else:
            has_more = res.has_more
        
        count += 1
    
    ids = []

    update_cursor(bank_register_id=bank_register_id, cursor=cursor)
    for trans in added:
        res = create_external_transaction(ExternalTransactionRequest(
            accountId=trans["account_id"],
            amount=trans["amount"],
            dateTransaction=trans["date"] ,
            merchantName=trans["merchant_name"],
            categoryPrimary=trans["personal_finance_category"]["primary"],
            categoryDetail=trans["personal_finance_category"]["detailed"],
            isTreated=isAllTreated
        ))
        ids.append(res['newId'])
    
    
    return ids


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
        external_transaction_fmt = [ ExternalTransactionRequest(
            accountId=trans["account_id"],
            amount=trans["amount"],
            dateTransaction=trans["date"] ,
            merchantName=trans["merchant_name"],
            categoryPrimary=trans["personal_finance_category"]["primary"],
            categoryDetail=trans["personal_finance_category"]["detailed"],
            isTreated=True
        ) for trans in transactions] 

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