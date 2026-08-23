import type { ListResponse } from "~/types/api";
import type { GetAccountResponse, GetAccountWithDetailResponse } from "~/types/api/account";
import type { AccountType } from "~/types/constants/account";
import type { List } from "~/types/ui";
import type { AccountCard, Account, AccountWithDetailType, QuickViewTransactionBalanceInfo, AccountCreditDetailType } from "~/types/ui/account";

export function accountWithDetailResponseToAccountWithDetail(data: GetAccountWithDetailResponse): AccountWithDetailType {
    return {
        ...data,
        type: data.type as AccountType,
        detail: data.detail ? {
            detailForCreditCard: data.detail.detailForCreditCard ? {
                ...data.detail.detailForCreditCard,
                nextInvoicePayment: new Date(data.detail.detailForCreditCard.nextInvoicePayment)
            } : undefined,
            detailForBroking: data.detail.detailForBroking,
            detailForChecking: data.detail.detailForChecking
        } : undefined
    }
}

export function listAccountsResponseToListAccountWithDetail(data: ListResponse<GetAccountWithDetailResponse>): List<AccountWithDetailType> {
    return {
        items: data.items.map(i => accountWithDetailResponseToAccountWithDetail(i)),
        total: data.total
    }
}

export function accountResponseToAccount(data: GetAccountResponse): Account {
    return {...data, type: data.type as AccountType}
}

export function listAccountsToListAccount(data: ListResponse<GetAccountResponse>): List<Account> {
    return {
        items: data.items.map(i => accountResponseToAccount(i)),
        total: data.total
    }
}

export function accountWithDetailToAccountCard(data: AccountWithDetailType, balanceHistory: number[]= []): AccountCard {
    return {
        id: data.id,
        title: data.title,
        balance: data.balance,
        color: data.color,
        type: data.type,
        balanceHistory: balanceHistory
    }
}

export function accountToAccountCard(data: Account, balanceHistory: number[]= []): AccountCard {
    return {
        id: data.id,
        title: data.title,
        color: data.color,
        balance: data.balance,
        type: data.type,
        balanceHistory: balanceHistory
    }
}

export function accountWithDetailToQuickInvoiceViewBalanceInfo(data: AccountWithDetailType): QuickViewTransactionBalanceInfo {
    return data
}