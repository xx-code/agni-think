import { RecordRepository } from "@core/repositories/recordRepository";
import { Record } from "@core/domains/entities/record";

export class MockRecordRepository implements RecordRepository {
    private records: Map<string, Record> = new Map();

    async save(request: Record): Promise<void> {
        this.records.set(request.getId(), request);
    }

    async get(id: string): Promise<Record> {
        const record = this.records.get(id);
        if (!record) {
            throw new Error(`Record with ID ${id} not found`);
        }
        return record;
    }

    async getAll(): Promise<Record[]> {
        return Array.from(this.records.values());
    }

    async getManyById(ids: string[]): Promise<Record[]> {
        return ids.map(id => {
            const record = this.records.get(id);
            if (!record) {
                throw new Error(`Record with ID ${id} not found`);
            }
            return record;
        });
    }

    async delete(id: string): Promise<void> {
        if (!this.records.has(id)) {
            throw new Error(`Record with ID ${id} not found`);
        }
        this.records.delete(id);
    }

    async update(request: Record): Promise<void> {
        if (!this.records.has(request.getId())) {
            throw new Error(`Record with ID ${request.getId()} not found`);
        }
        this.records.set(request.getId(), request);
    }
}
