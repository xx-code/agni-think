import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction"

export interface ScheduleTransactionRepository {
    existById(id: string): Promise<boolean>
    existByName(name: string): Promise<boolean>
    create(scheduleTransaction: ScheduleTransaction): Promise<void>
    get(scheduleTransactionId: string): Promise<ScheduleTransaction|null>
    getAll(): Promise<ScheduleTransaction[]>
    update(scheduleTransaction: ScheduleTransaction): Promise<void>
    delete(scheduleTransactionId: string): Promise<void>
}