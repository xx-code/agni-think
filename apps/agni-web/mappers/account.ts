import type { AccountCard, Account, AccountWithDetailType } from "~/types/ui/account";

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