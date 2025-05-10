import { SaveGoal } from "@core/domains/entities/saveGoal"


export interface SavingRepository {
    create(saveGoal: SaveGoal): Promise<void>
    get(saveGoalId: string): Promise<SaveGoal>
    getAll(): Promise<SaveGoal[]>
    update(saveGoal: SaveGoal): Promise<void>
    delete(saveGoalId: string): Promise<void>
}