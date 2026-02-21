import type { GetAccountResponse, GetAccountWithDetailResponse } from "~/types/api/account";
import type { AccountBrokeDetailType, AccountCheckingDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType } from "~/types/ui/account";
import type { UseApiFetchReturn } from "~/types/utils";

export async function fetchAccount(accountId: string): Promise<AccountType> {
    const res = await $fetch<GetAccountResponse>(`/api/accounts/${accountId}`, {
        method: 'GET'
    });
    return {
        id: res.id,
        title: res.title,
        balance: res.balance,
        type: res.type
    };
}

export async function fetchAccountWithDetail(accountId: string): Promise<AccountWithDetailType> {
    const res = await $fetch<GetAccountWithDetailResponse>(`/api/accounts/${accountId}/getAccountWithDetail`, {
        method: 'GET'
    });

    const detailAccount = (type: string) => {
        switch(type) {
            case 'Broking':
                return {
                    managementType: res.detail.detailForBroking?.contributionType ?? "",
                    type: res.detail.detailForBroking?.managementType ?? ""
                } satisfies AccountBrokeDetailType 
            case 'CreditCard':    
                return {
                    creditUtilisation: res.detail.detailForCreditCard?.creditUtilisation ?? 0,
                    creditLimit: res.detail.detailForCreditCard?.creditCardLimit ?? 0,
                    nextInvoicePaymentDate: new Date(res.detail.detailForCreditCard?.nextInvoicePayment ?? Date.now().toString())
                } satisfies AccountCreditDetailType
            case 'Checking':
                return {
                    buffer: res.detail.detailForChecking?.buffer ?? 0
                } satisfies AccountCheckingDetailType
            default:
                return undefined
        }
    }
    return {
        id: res.id,
        title: res.title,
        balance: res.balance,
        type: res.type,
        lockedBalance: res.lockedBalance,
        freezedBalance: res.freezeBalance,
        detail: detailAccount(res.type) 
    };
}