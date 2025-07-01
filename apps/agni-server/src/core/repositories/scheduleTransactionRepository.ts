import { ScheduleTransaction } from "@core/domains/entities/ScheduleTransaction"

export interface ScheduleTransactionRepository {
    create(saveGoal: ScheduleTransaction): Promise<void>
    get(saveGoalId: string): Promise<ScheduleTransaction|null>
    getAll(): Promise<ScheduleTransaction[]>
    update(saveGoal: ScheduleTransaction): Promise<void>
    delete(saveGoalId: string): Promise<void>
}