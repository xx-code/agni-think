import { TransactionPaginationResponse } from "../domains/metaData/transaction";
import { Money } from "../domains/entities/money";
import { Transaction } from "../domains/entities/transaction";
import { TransactionMainCategory } from "@core/domains/constants";

export type TransactionFilter = {
    accounts: Array<string>;
    categories: Array<string>;
    budgets: Array<string>,
    tags: Array<string>;
    startDate: string
    endDate: string
    types: TransactionMainCategory[],
    minPrice: Money | null
    maxPrice: Money | null
}

export type SortBy = {
    sortBy: string,
    asc: boolean
}


export interface TransactionRepository {
    save(request: Transaction): Promise<void>;
    get(id: string): Promise<Transaction>;
    isTransactionExistById(id: string): Promise<boolean>
    getPaginations(page:number, size: number, sortBy: SortBy|null, filterBy: TransactionFilter): Promise<TransactionPaginationResponse>;
    getTransactions(filterBy: TransactionFilter): Promise<Transaction[]>
    delete(id: string): Promise<void>;
    update(request: Transaction): Promise<void>;
}