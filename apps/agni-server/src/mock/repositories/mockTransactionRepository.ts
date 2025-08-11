import { Money } from "@core/domains/entities/money";
import { Transaction } from "@core/domains/entities/transaction";
import { RepositoryListResult } from "@core/repositories/dto";
import { SortBy, TransactionFilter, TransactionRepository } from "@core/repositories/transactionRepository";

export class MockTransactionRepository implements TransactionRepository {
    private transactions: Map<string, Transaction> = new Map();

    async save(request: Transaction): Promise<void> {
        this.transactions.set(request.getId(), request);
    }

    async get(id: string): Promise<Transaction> {
        const transaction = this.transactions.get(id);
        if (!transaction) {
            throw new Error(`Transaction with ID ${id} not found`);
        }
        return transaction;
    }

    async isTransactionExistById(id: string): Promise<boolean> {
        return this.transactions.has(id);
    }

    async getPaginations(page: number, size: number, sortBy: SortBy | null, filterBy: TransactionFilter): Promise<RepositoryListResult<Transaction>> {
        let filteredTransactions = Array.from(this.transactions.values());

        // Appliquer les filtres (exemple simple : filtrage par comptes)
        if (filterBy.accounts && filterBy.accounts?.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => filterBy.accounts?.includes(tr.getAccountRef()));
        }
        if (filterBy.categories && filterBy.categories.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => filterBy.categories?.includes(tr.getCategoryRef()));
        }
        if (filterBy.tags && filterBy.tags.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => tr.getTags().forEach(tag => filterBy.tags?.includes(tag)));
        }
        if (filterBy.budgets && filterBy.budgets.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => tr.getBudgetRefs().forEach(budget => filterBy.budgets?.includes(budget)));
        }
        // Pagination
        const start = (page - 1) * size;
        const end = start + size;
        const paginatedData = filteredTransactions.slice(start, end);

        return {
            items: paginatedData,
            total: Number((filteredTransactions.length / page).toFixed(0))
        };
    }

    async getTransactions(filterBy: TransactionFilter): Promise<Transaction[]> {
        let filteredTransactions = Array.from(this.transactions.values());

        // Appliquer les filtres (exemple simple : filtrage par comptes)
        if (filterBy.accounts && filterBy.accounts?.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => filterBy.accounts?.includes(tr.getAccountRef()));
        }
        if (filterBy.categories && filterBy.categories.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => filterBy.categories?.includes(tr.getCategoryRef()));
        }
        if (filterBy.tags && filterBy.tags.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => tr.getTags().forEach(tag => filterBy.tags?.includes(tag)));
        }
        if (filterBy.budgets && filterBy.budgets.length > 0) {
            filteredTransactions = filteredTransactions.filter(tr => tr.getBudgetRefs().forEach(budget => filterBy.budgets?.includes(budget)));
        }

        return filteredTransactions
    }


    async delete(id: string): Promise<void> {
        if (!this.transactions.has(id)) {
            throw new Error(`Transaction with ID ${id} not found`);
        }
        this.transactions.delete(id);
    }

    async update(request: Transaction): Promise<void> {
        if (!this.transactions.has(request.getId())) {
            throw new Error(`Transaction with ID ${request.getId()} not found`);
        }
        this.transactions.set(request.getId(), request);
    }
}
