import { Record } from '../domains/entities/record';
import { QueryFilterAllRepository, RepositoryListResult } from './dto';

export interface RecordRepository {
    save(request: Record): Promise<void>
    get(id: string): Promise<Record|null>;
    getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<Record>>;
    getManyById(ids: string[]): Promise<Record[]>;
    delete(id: string): Promise<void>;
    update(request: Record): Promise<void>;
}