import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateAccountRequest, GetAccountResponse, GetAccountWithDetailResponse, UpdateAccountRequest } from "~/types/api/account";
import { accountResponseToAccount, accountWithDetailResponseToAccountWithDetail, listAccountsToListAccount, listAccountsResponseToListAccountWithDetail } from "~/mappers/account";
import type { Account, AccountWithDetailType } from "~/types/ui/account";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchAccount(accountId: string): Promise<Account> {
    return await ApiLinkBuilder
        .route<GetAccountResponse>(API_ROUTES.ACCOUNTS.GET_ACCOUNT)
        .params({ id: accountId })
        .mapper(accountResponseToAccount)
        .execute()
}

export async function fetchAccountWithDetail(accountId: string): Promise<AccountWithDetailType> {
    return await ApiLinkBuilder
        .route<GetAccountWithDetailResponse>(API_ROUTES.ACCOUNTS.GET_ACCOUNT)
        .params({ id: accountId })
        .query({ withDetail: true })
        .mapper(accountWithDetailResponseToAccountWithDetail)
        .execute()
}

export const ALL_ACCOUNT_ID = "all";

export async function fetchAccounts(query: QueryFilterRequest): Promise<ListResponse<Account>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
        .query(query)
        .mapper(listAccountsToListAccount)
        .execute()
}

export async function fetchAccountsWithDetail(query: QueryFilterRequest): Promise<ListResponse<AccountWithDetailType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetAccountWithDetailResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
        .query({ ...query, withDetail: true })
        .mapper(listAccountsResponseToListAccountWithDetail)
        .execute()
}

export async function createAccount(request: CreateAccountRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.ACCOUNTS.CREATE_ACCOUNT)
        .body(request)
        .execute()
}

export async function deleteAccount(accountId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.ACCOUNTS.DELETE_ACCOUNT)
        .params({ id: accountId })
        .execute()
}

export async function updateAccount(accountId: string, request: UpdateAccountRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.ACCOUNTS.UPDATE_ACCOUNT)
        .params({ id: accountId })
        .body(request)
        .execute()
}
