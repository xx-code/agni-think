import { Patrimony } from "@core/domains/entities/patrimony";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import { ListDto } from "@core/dto/base";
import { RepositoryListResult } from "./dto";

export interface PatrimonyRepository {
    save(patrimony: Patrimony): Promise<void>
    exist(id: string): Promise<boolean>
    existByName(name: string): Promise<boolean>
    get(id: string): Promise<Patrimony|null>;
    getAll(): Promise<RepositoryListResult<Patrimony>>;
    getManyById(ids: string[]): Promise<Patrimony[]>;
    delete(id: string): Promise<void>;
    update(patrimony: Patrimony): Promise<void>;
}

export type PatrimonyTransactionFilter = {
    patrimonyIds?: Array<string> 
    startDate?: Date
    endDate?: Date
    sort?: 'asc' | 'desc'
}

export interface PatrimonySnapshotRepository {
    save(transaction: PatrimonySnapshot): Promise<void>
    exist(id: string): Promise<boolean>
    get(id: string): Promise<PatrimonySnapshot|null>
    getLastest(filter: PatrimonyTransactionFilter): Promise<PatrimonySnapshot[]>
    getAll(filter: PatrimonyTransactionFilter): Promise<RepositoryListResult<PatrimonySnapshot>>
    getManyById(ids: string[]): Promise<PatrimonySnapshot[]>
    delete(id: string): Promise<void>
    deleteByIds(ids: string[]): Promise<void>
    update(transaction: PatrimonySnapshot): Promise<void>
}