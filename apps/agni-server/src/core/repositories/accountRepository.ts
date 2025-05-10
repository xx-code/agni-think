import { Account } from "../domains/entities/account";

export interface AccountRepository {
    save(account: Account): Promise<void>
    isExistByName(account_title: string): Promise<boolean>
    isExistById(id: string): Promise<boolean>
    isExistByIds(ids: string[]): Promise<boolean>
    get(id: string): Promise<Account>
    getAll(): Promise<Account[]>
    delete(id: string): Promise<void>
    update(account: Account): Promise<void>
}