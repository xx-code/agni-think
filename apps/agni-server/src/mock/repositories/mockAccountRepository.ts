import { Account } from "@core/domains/entities/account";
import { AccountRepository } from "@core/repositories/accountRepository";

export class MockAccountRepository implements AccountRepository {
    getManyIds(ids: string[]): Promise<Account[]> {
        throw new Error("Method not implemented.");
    }
    private accounts: Map<string, Account> = new Map();

    async save(account: Account): Promise<void> {
        this.accounts.set(account.getId(), account);
    }

    async isExistByName(account_title: string): Promise<boolean> {
        for (const account of this.accounts.values()) {
            if (account.getTitle() === account_title) {
                return true;
            }
        }
        return false;
    }

    async isExistById(id: string): Promise<boolean> {
        return this.accounts.has(id);
    }

    async isExistByIds(ids: string[]): Promise<boolean> {
        return ids.every(id => this.accounts.has(id));
    }

    async get(id: string): Promise<Account> {
        const account = this.accounts.get(id);
        if (!account) {
            throw new Error(`Account with id ${id} not found`);
        }
        return account;
    }

    async getAll(): Promise<Account[]> {
        return Array.from(this.accounts.values());
    }

    async delete(id: string): Promise<void> {
        this.accounts.delete(id);
    }

    async update(account: Account): Promise<void> {
        if (!this.accounts.has(account.getId())) {
            throw new Error(`Account with id ${account.getId()} not found`);
        }
        this.accounts.set(account.getId(), account);
    }
}
