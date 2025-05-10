import { SaveGoal } from "@core/domains/entities/saveGoal";
import { ValueError } from "@core/errors/valueError";
import { SavingRepository } from "@core/repositories/savingRepository";

export class MockSavingRepository implements SavingRepository {
    private saveGoals: Map<string, SaveGoal> = new Map();

    async create(saveGoal: SaveGoal): Promise<void> {
        if (this.saveGoals.has(saveGoal.getId())) {
            throw new ValueError(`SaveGoal with ID ${saveGoal.getId()} already exists.`);
        }
        this.saveGoals.set(saveGoal.getId(), saveGoal);
    }

    async get(saveGoalId: string): Promise<SaveGoal> {
        const saveGoal = this.saveGoals.get(saveGoalId);
        if (!saveGoal) {
            throw new ValueError(`SaveGoal with ID ${saveGoalId} not found.`);
        }
        return saveGoal;
    }

    async getAll(): Promise<SaveGoal[]> {
        return Array.from(this.saveGoals.values());
    }

    async update(saveGoal: SaveGoal): Promise<void> {
        if (!this.saveGoals.has(saveGoal.getId())) {
            throw new ValueError(`SaveGoal with ID ${saveGoal.getId()} not found.`);
        }
        this.saveGoals.set(saveGoal.getId(), saveGoal);
    }

    async delete(saveGoalId: string): Promise<void> {
        if (!this.saveGoals.has(saveGoalId)) {
            throw new ValueError(`SaveGoal with ID ${saveGoalId} not found.`);
        }
        this.saveGoals.delete(saveGoalId);
    }
}
