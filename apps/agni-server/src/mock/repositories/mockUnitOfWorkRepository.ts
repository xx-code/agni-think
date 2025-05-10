import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";

export class MockUnitOfWork implements UnitOfWorkRepository {

    async start(): Promise<void> {
        return 
    }
    async commit(): Promise<void> {
        return 
    }
    async rollback(): Promise<void> {
        return 
    }
}