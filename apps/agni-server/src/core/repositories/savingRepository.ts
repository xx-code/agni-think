import { SaveGoal } from "@core/domains/entities/saveGoal"
import { QueryFilterAllRepository, RepositoryListResult } from "./dto"

export interface SavingRepository {
    create(saveGoal: SaveGoal): Promise<void>
    get(saveGoalId: string): Promise<SaveGoal|null>
    getAll(query: QueryFilterAllRepository): Promise<RepositoryListResult<SaveGoal>>
    update(saveGoal: SaveGoal): Promise<void>
    delete(saveGoalId: string): Promise<void>
}