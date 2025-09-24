import { Money } from "../domains/entities/money";
import { Transaction } from "../domains/entities/transaction";
import { TransactionStatus, TransactionType } from "@core/domains/constants";
import { QueryFilterAllRepository, RepositoryListResult, SortBy } from "./dto";

export type TransactionFilter = QueryFilterAllRepository & {
    accounts?: Array<string>;
    categories?: Array<string>;
    budgets?: Array<string>,
    tags?: Array<string>;
    startDate?: Date
    endDate?: Date
    strictEndDate?: boolean
    strictStartDate?: boolean
    status?: TransactionStatus
    types?: TransactionType[],
    minPrice?: Money
    maxPrice?: Money
    isFreeze?: boolean
    queryAll?: boolean
}


export interface TransactionRepository {
    save(request: Transaction): Promise<void>;
    get(id: string): Promise<Transaction>;
    isTransactionExistById(id: string): Promise<boolean>
    getPaginations(filterBy: TransactionFilter): Promise<RepositoryListResult<Transaction>>;
    getTransactions(filterBy: TransactionFilter): Promise<Transaction[]>
    delete(id: string): Promise<void>;
    update(request: Transaction): Promise<void>;
}