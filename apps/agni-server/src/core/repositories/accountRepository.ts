import { Account } from "../domains/entities/account";
import { QueryFilterAllRepository, RepositoryListResult } from "./dto";

export interface AccountRepository {
    save(account: Account): Promise<void>
    isExistByName(account_title: string): Promise<boolean>
    isExistById(id: string): Promise<boolean>
    isExistByIds(ids: string[]): Promise<boolean>
    get(id: string): Promise<Account|null>
    getManyIds(ids: string[]): Promise<Account[]>
    getAll(filter: QueryFilterAllRepository): Promise<RepositoryListResult<Account>>
    delete(id: string): Promise<void>
    update(account: Account): Promise<void>
}