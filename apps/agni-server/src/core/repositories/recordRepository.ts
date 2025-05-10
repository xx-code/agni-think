import { Record } from '../domains/entities/record';

export interface RecordRepository {
    save(request: Record): Promise<void>
    get(id: string): Promise<Record>;
    getAll(): Promise<Record[]>;
    getManyById(ids: string[]): Promise<Record[]>;
    delete(id: string): Promise<void>;
    update(request: Record): Promise<void>;
}