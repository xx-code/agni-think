import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction"
import { QueryFilterAllRepository, RepositoryListResult } from "./dto"

export interface ScheduleTransactionRepository {
    existById(id: string): Promise<boolean>
    existByName(name: string): Promise<boolean>
    create(scheduleTransaction: ScheduleTransaction): Promise<void>
    get(scheduleTransactionId: string): Promise<ScheduleTransaction|null>
    getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<ScheduleTransaction>>
    update(scheduleTransaction: ScheduleTransaction): Promise<void>
    delete(scheduleTransactionId: string): Promise<void>
}