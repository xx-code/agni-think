import { TransactionPaginationResponse } from "../domains/metaData/transaction";
import { Money } from "../domains/entities/money";
import { TransactionType } from "../domains/entities/record";
import { Transaction } from "../domains/entities/transaction";

export type TransactionFilter = {
    accounts: Array<string>;
    categories: Array<string>;
    tags: Array<string>;
    startDate: string
    endDate: string
    minPrice: Money | null
    maxPrice: Money | null
    type: TransactionType | null
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