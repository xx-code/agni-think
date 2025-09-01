import { Budget } from "../domains/entities/budget";
import { QueryFilterAllRepository, RepositoryListResult } from "./dto";


export interface BudgetRepository {
    isBudgetExistById(id: string): Promise<boolean>
    isBudgetExistByIds(ids: string[]): Promise<boolean>
    isBudgetExistByName(title: string): Promise<boolean>
    save(request: Budget): Promise<void>;
    get(id: string): Promise<Budget>;
    getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<Budget>>;
    delete(id: string): Promise<void>;
    toggleArchived(id: string, doArchive: boolean): Promise<void>;
    update(request: Budget): Promise<void>;
}