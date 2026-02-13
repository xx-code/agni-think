export interface UnitOfWorkRepository {
    start(): Promise<void>
    commit(): Promise<void>
    rollback(): Promise<void>
}