import { Transaction } from "../entities/transaction";

export type TransactionPaginationResponse = {
    transactions: Transaction[];
    currentPage: number;
    maxPage: number;
}