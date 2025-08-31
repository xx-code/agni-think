import { SaveGoal } from "@core/domains/entities/saveGoal"
import { QueryFilterAllRepository } from "./dto"

export interface SavingRepository {
    create(saveGoal: SaveGoal): Promise<void>
    get(saveGoalId: string): Promise<SaveGoal|null>
    getAll(query: QueryFilterAllRepository): Promise<SaveGoal[]>
    update(saveGoal: SaveGoal): Promise<void>
    delete(saveGoalId: string): Promise<void>
}