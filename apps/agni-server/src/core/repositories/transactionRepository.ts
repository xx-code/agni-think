import { Money } from "../domains/entities/money";
import { Transaction } from "../domains/entities/transaction";
import { TransactionType } from "@core/domains/constants";
import { RepositoryListResult } from "./dto";

export type TransactionFilter = {
    accounts?: Array<string>;
    categories?: Array<string>;
    budgets?: Array<string>,
    tags?: Array<string>;
    startDate?: Date
    endDate?: Date
    types?: TransactionType[],
    minPrice?: Money
    maxPrice?: Money
    isFreeze?: boolean
    queryAll?: boolean
}

export type SortBy = {
    sortBy: string,
    asc: boolean
}


export interface TransactionRepository {
    save(request: Transaction): Promise<void>;
    get(id: string): Promise<Transaction>;
    isTransactionExistById(id: string): Promise<boolean>
    getPaginations(offset:number, size: number, sortBy: SortBy|null, filterBy: TransactionFilter): Promise<RepositoryListResult<Transaction>>;
    getTransactions(filterBy: TransactionFilter): Promise<Transaction[]>
    delete(id: string): Promise<void>;
    update(request: Transaction): Promise<void>;
}